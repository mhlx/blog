package me.qyh.blog.utils;

import org.springframework.context.i18n.LocaleContextHolder;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;


/**
 * A utility that simplifies in-memory compilation of new classes.
 *
 * @author Lukas Eder
 */
public class Compile {

    public static Class<?> compile(String className, String content) {
        Lookup lookup = MethodHandles.lookup();
        ClassLoader cl = lookup.lookupClass().getClassLoader();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<CharSequenceJavaFileObject> files = new ArrayList<>();
        files.add(new CharSequenceJavaFileObject(className, content));
        StringWriter out = new StringWriter();
        StringBuilder classpath = new StringBuilder();
        String separator = System.getProperty("path.separator");
        String cp = System.getProperty("java.class.path");
        String mp = System.getProperty("jdk.module.path");
        if (cp != null && !"".equals(cp))
            classpath.append(cp);
        if (mp != null && !"".equals(mp))
            classpath.append(mp);
        if (cl instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) cl).getURLs()) {
                if (classpath.length() > 0)
                    classpath.append(separator);

                if ("file".equals(url.getProtocol())) {
                    try {
                        classpath.append(new File(url.toURI()));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        List<String> options = new ArrayList<>(Arrays.asList("-classpath", classpath.toString()));
        DiagnosticCollector<FileObject> listener = new DiagnosticCollector<>();

        try (ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null))) {
            CompilationTask task = compiler.getTask(out, fileManager, listener, options, null, files);

            if (!task.call()) {
                List<CompileError> errors = new ArrayList<>();
                for (Diagnostic<? extends FileObject> diagnostic : listener.getDiagnostics()) {
                    errors.add(new CompileError(diagnostic.getMessage(LocaleContextHolder.getLocale()), diagnostic.getLineNumber(), diagnostic.getColumnNumber()));
                }
                throw new CompileException(errors);
            }

            if (fileManager.isEmpty())
                throw new RuntimeException("Compilation error: " + out);

            ByteArrayClassLoader c = new ByteArrayClassLoader(fileManager.classes());
            return fileManager.loadAndReturnMainClass(className,
                    (name, bytes) -> {
                        try {
                            return c.loadClass(name);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* [java-9] */
    static final class ByteArrayClassLoader extends ClassLoader {
        private final Map<String, byte[]> classes;

        ByteArrayClassLoader(Map<String, byte[]> classes) {
            super(ByteArrayClassLoader.class.getClassLoader());

            this.classes = classes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classes.get(name);

            if (bytes == null)
                return super.findClass(name);
            else
                return defineClass(name, bytes, 0, bytes.length);
        }
    }

    private static final class JavaFileObject extends SimpleJavaFileObject {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        JavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        byte[] getBytes() {
            return os.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() {
            return os;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, JavaFileObject> fileObjectMap;
        private Map<String, byte[]> classes;

        ClassFileManager(StandardJavaFileManager standardManager) {
            super(standardManager);

            fileObjectMap = new HashMap<>();
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
                JavaFileManager.Location location,
                String className,
                JavaFileObject.Kind kind,
                FileObject sibling
        ) {
            JavaFileObject result = new JavaFileObject(className, kind);
            fileObjectMap.put(className, result);
            return result;
        }

        boolean isEmpty() {
            return fileObjectMap.isEmpty();
        }

        Map<String, byte[]> classes() {
            if (classes == null) {
                classes = new HashMap<>();

                for (Entry<String, JavaFileObject> entry : fileObjectMap.entrySet())
                    classes.put(entry.getKey(), entry.getValue().getBytes());
            }

            return classes;
        }

        Class<?> loadAndReturnMainClass(String mainClassName, ThrowingBiFunction<String, byte[], Class<?>> definer) {
            for (Entry<String, byte[]> entry : classes().entrySet()) {
                String className = entry.getKey();
                int index$ = className.indexOf('$');
                if (index$ != -1) {
                   continue;
                }
                if (className.contains(mainClassName))
                    return definer.apply(entry.getKey(), entry.getValue());
            }
            return null;
        }
    }

    @FunctionalInterface
    interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u);
    }

    private static final class CharSequenceJavaFileObject extends SimpleJavaFileObject {
        final CharSequence content;

        public CharSequenceJavaFileObject(String className, CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }


    public static class CompileError {
        private final String message;
        private final long line;
        private final long col;

        private CompileError(String message, long line, long col) {
            this.message = message;
            this.line = line;
            this.col = col;
        }

        public String getMessage() {
            return message;
        }

        public long getLine() {
            return line;
        }

        public long getCol() {
            return col;
        }

        @Override
        public String toString() {
            return "CompileError{" +
                    "message='" + message + '\'' +
                    ", line=" + line +
                    ", col=" + col +
                    '}';
        }
    }

    public static class CompileException extends RuntimeException {
        private final List<CompileError> errors;

        public CompileException(List<CompileError> errors) {
            this.errors = errors;
        }

        public List<CompileError> getErrors() {
            return errors;
        }
    }
}
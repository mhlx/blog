package me.qyh.blog.web.template;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.engine.TemplateData;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

final class ThymeleafWebEngineContext extends WebEngineContext {

    private ParamMap paramMap;

    public ThymeleafWebEngineContext(IEngineConfiguration configuration, TemplateData templateData,
                                     Map<String, Object> templateResolutionAttributes, HttpServletRequest request, HttpServletResponse response,
                                     ServletContext servletContext, Locale locale, Map<String, Object> variables) {
        super(configuration, templateData, templateResolutionAttributes, request, response, servletContext, locale,
                variables);
    }

    @Override
    public Object getVariable(String key) {
        if ("param".equals(key)) {
            if (paramMap == null) {
                paramMap = new ParamMap(getRequest());
            }
            return paramMap;
        }
        return super.getVariable(key);
    }

    private final class ParamMap implements Map<String, List<String>> {

        private final HttpServletRequest request;
        private Map<String, List<String>> map;

        ParamMap(final HttpServletRequest request) {
            super();
            this.request = request;
        }

        @Override
        public int size() {
            return request.getParameterMap().size();
        }

        @Override
        public boolean isEmpty() {
            return this.request.getParameterMap().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return true;
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> get(Object key) {
            final String[] values = this.request.getParameterValues(key == null ? null : key.toString());
            if (values == null) {
                return null;
            }
            return new Param(values);
        }

        @Override
        public List<String> put(String key, List<String> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends List<String>> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            return this.request.getParameterMap().keySet();
        }

        @Override
        public Collection<List<String>> values() {
            setMap();
            return this.map.values();
        }

        @Override
        public Set<Map.Entry<String, List<String>>> entrySet() {
            setMap();
            return this.map.entrySet();
        }

        private void setMap() {
            if (this.map == null) {
                this.map = new HashMap<>();
                for (Map.Entry<String, String[]> it : this.request.getParameterMap().entrySet()) {
                    this.map.put(it.getKey(), List.of(it.getValue()));
                }
            }
        }
    }

    private final class Param extends ArrayList<String> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final String[] values;

        public Param(String[] values) {
            super();
            this.values = values;
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean isEmpty() {
            return values.length == 0;
        }

        @Override
        public String get(int index) {
            return values[index];
        }

        @Override
        public String toString() {
            if (values.length == 0) {
                return "";
            }
            return values[0];
        }

        @Override
        public Iterator<String> iterator() {
            return Arrays.stream(values).iterator();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Arrays.hashCode(values);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (values != null && values.length == 1 && (obj instanceof String)) {
                return values[0].equals(obj);
            }
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass()) {
                return false;
            }
            Param other = (Param) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (!Arrays.equals(values, other.values))
                return false;
            return true;
        }

        private ThymeleafWebEngineContext getEnclosingInstance() {
            return ThymeleafWebEngineContext.this;
        }
    }
}

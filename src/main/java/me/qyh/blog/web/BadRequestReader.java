package me.qyh.blog.web;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import me.qyh.blog.FieldMessageSourceResolvable;
import me.qyh.blog.Message;
import me.qyh.blog.exception.BadRequestException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class BadRequestReader {

    private static final List<Class<?>> classes = List.of(BindException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            TypeMismatchException.class, ServletRequestBindingException.class, InvalidPropertyException.class,
            UnsatisfiedServletRequestParameterException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, BadRequestException.class, HttpMediaTypeNotAcceptableException.class);

    public static boolean isBadRequestException(Throwable ex) {
        return classes.stream().anyMatch(clazz -> clazz.isAssignableFrom(ex.getClass()));
    }

    private BadRequestReader() {
        super();
    }

    public static List<MessageSourceResolvable> readErrors(Throwable ex) {
        List<MessageSourceResolvable> errors = new ArrayList<>();
        // print error with some ex
        if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException _ex = (MissingServletRequestParameterException) ex;
            errors.add(new Message("parameter.miss", _ex.getParameterName() + "参数缺失", _ex.getParameterName()));
        }
        if (ex instanceof UnsatisfiedServletRequestParameterException) {
            errors.add(new Message("parameter.unmatch", "参数不匹配"));
        }
        if (ex instanceof HttpMessageNotReadableException) {
            Throwable cause = ex.getCause();
            if (cause instanceof MismatchedInputException) {
                String fieldNames = getFieldNames((MismatchedInputException) cause);
                if (fieldNames != null && !fieldNames.isEmpty()) {

                    if (cause instanceof InvalidFormatException) {
                        errors.add(new FieldMessageSourceResolvable("bind.field.invalidFormat", fieldNames + "数据格式不正确", fieldNames, fieldNames));
                    } else {
                        errors.add(new FieldMessageSourceResolvable("bind.field.typemismatch", fieldNames + "类型不正确", fieldNames, fieldNames));
                    }
                }
            }
        }
        if (ex instanceof MethodArgumentNotValidException) {
            readErrors(((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors(), errors);
        }
        if (ex instanceof BindException) {
            readErrors(((BindException) ex).getAllErrors(), errors);
        }

        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException _ex = (ConstraintViolationException) ex;
            if (_ex.getConstraintViolations() != null) {
                _ex.getConstraintViolations().stream().map(cv -> {
                    String code = cv.getRootBeanClass().getName() + "." + cv.getPropertyPath().toString();
                    return new Message(code, cv.getMessage(), cv.getInvalidValue());
                }).forEach(errors::add);
            }
        }

        if (ex instanceof BadRequestException) {
            errors.add(((BadRequestException) ex).getError());
        }
        return errors;
    }

    private static void readErrors(List<ObjectError> errors, List<MessageSourceResolvable> messages) {
        for (ObjectError e : errors) {
            if (e.contains(TypeMismatchException.class)) {
                if (e instanceof FieldError) {
                    FieldError fe = (FieldError) e;
                    messages.add(new FieldMessageSourceResolvable("bind.field.typemismatch", fe.getField() + "类型不正确", fe.getField(), fe.getField()));
                }
            } else if (e.contains(MethodInvocationException.class)) {// method Invocation Exception ??
                if (e instanceof FieldError) {
                    FieldError fe = (FieldError) e;
                    messages.add(new FieldMessageSourceResolvable("bind.field.invalidValue", "值无效", fe.getField(), fe.getField()));
                }
            } else {
                if (e instanceof FieldError) {
                    FieldError fe = (FieldError) e;
                    messages.add(new FieldMessageSourceResolvable(fe.getCode(), fe.getDefaultMessage(), fe.getField(), fe.getArguments()));
                } else {
                    messages.add(e);
                }
            }
        }
    }

    private static String getFieldNames(JsonMappingException ex) {
        return ex.getPath().stream().map(Reference::getFieldName).collect(Collectors.joining("."));
    }
}

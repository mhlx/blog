package me.qyh.blog.web.template.tag;

import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import me.qyh.blog.web.BlogMessageCodeResolver;

public abstract class DataProviderSupport<T> extends DataProvider<T> {

	@Autowired(required = false)
	private Validator validator;

	public DataProviderSupport(String name) {
		super(name);
	}

	protected <E> E bind(E e, Map<String, String> attributesMap) throws BindException {
		MutablePropertyValues mvps = new MutablePropertyValues(attributesMap);
		DataBinder binder = new DataBinder(e, getObjectName());
		binder.setMessageCodesResolver(BlogMessageCodeResolver.INSTANCE);
		binder.setIgnoreInvalidFields(false);
		binder.bind(mvps);
		if (validator != null) {
			binder.setValidator(validator);
			binder.validate();
			if (binder.getBindingResult().hasErrors()) {
				throw new BindException(binder.getBindingResult());
			}
		}
		return e;
	}

	protected BindingResult createBindingResult(Map<String, String> attributeMap) {
		MapBindingResult br = new MapBindingResult(attributeMap, getObjectName());
		br.setMessageCodesResolver(BlogMessageCodeResolver.INSTANCE);
		return br;
	}

	protected String getObjectName() {
		return getName() + "DataTag";
	}

}

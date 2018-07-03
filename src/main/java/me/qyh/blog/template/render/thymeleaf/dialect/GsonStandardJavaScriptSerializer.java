package me.qyh.blog.template.render.thymeleaf.dialect;

import java.io.Writer;

import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.serializer.IStandardJavaScriptSerializer;

import me.qyh.blog.core.util.Jsons;

public class GsonStandardJavaScriptSerializer implements IStandardJavaScriptSerializer {

	@Override
	public void serializeValue(Object object, Writer writer) {
		try {
			Jsons.write(object, writer);
		} catch (final Exception e) {
			throw new TemplateProcessingException(
					"An exception was raised while trying to serialize object to JavaScript using Gson", e);
		}
	}

}

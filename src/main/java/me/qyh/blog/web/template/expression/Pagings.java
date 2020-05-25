package me.qyh.blog.web.template.expression;

import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import me.qyh.blog.vo.PageResult;

final class Pagings {

	public OfInt step(PageResult<?> page, int step) {
		int offset = step % 2 == 0 ? step / 2 : (step - 1) / 2;
		int first, last;
		if (page.getCurrentPage() - offset < 1) {
			first = 1;
			last = step;
		} else {
			first = page.getCurrentPage() - offset;
			last = page.getCurrentPage() + (step % 2 == 0 ? offset - 1 : offset);
		}
		last = Math.min(last, page.getTotalPage());
		if (last - first + 1 < step) {
			if (page.getTotalPage() > step) {
				first = last - step + 1;
			} else {
				first = 1;
			}
		}
		return IntStream.range(first, last + 1).iterator();
	}

}

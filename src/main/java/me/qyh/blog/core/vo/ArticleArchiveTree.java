/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.core.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import me.qyh.blog.core.entity.Article;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.Times;

public class ArticleArchiveTree {

	/**
	 * 文章排序，按照发布时间倒叙排，如果发布时间相同，按照ID倒序排.
	 */
	private static final Comparator<Article> articleComparator = Comparator.comparing(Article::getPubDate).reversed()
			.thenComparing(Comparator.comparing(Article::getId).reversed());

	public enum ArticleArchiveMode {
		YMD, YM, Y
	}

	private final List<DateNode> nodes;

	public ArticleArchiveTree(List<Article> articles, ArticleArchiveMode mode) {
		if (!CollectionUtils.isEmpty(articles)) {
			Set<LocalDate> dates = articles.stream().map(this::getLocalDate).collect(Collectors.toSet());
			Map<Integer, List<LocalDate>> yearDates = dates.stream().collect(Collectors.groupingBy(LocalDate::getYear));
			nodes = new ArrayList<>(yearDates.size());
			switch (mode) {
			case Y:
				for (Integer year : yearDates.keySet()) {
					DateNode node = new YearDateNode(year, new Message("archive.year", year + "年", year));
					setArticles(node, articles);
					nodes.add(node);
				}
				break;
			case YM:
				for (Map.Entry<Integer, List<LocalDate>> it : yearDates.entrySet()) {
					int year = it.getKey();
					DateNode yearNode = new YearDateNode(year, new Message("archive.year", year + "年", year));
					Set<Integer> months = it.getValue().stream()
							.collect(Collectors.groupingBy(LocalDate::getMonthValue)).keySet();
					List<DateNode> monthNodes = new ArrayList<>();
					for (Integer month : months) {
						DateNode monthNode = new MonthDateNode(year, month,
								new Message("archive.month", month + "月", month));
						setArticles(monthNode, articles);
						monthNodes.add(monthNode);
					}
					Collections.sort(monthNodes);
					yearNode.addAll(monthNodes);
					nodes.add(yearNode);
				}
				break;
			case YMD:
				for (Map.Entry<Integer, List<LocalDate>> it : yearDates.entrySet()) {
					int year = it.getKey();
					DateNode yearNode = new YearDateNode(year, new Message("archive.year", year + "年", year));
					Map<Integer, List<LocalDate>> months = it.getValue().stream()
							.collect(Collectors.groupingBy(LocalDate::getMonthValue));
					List<DateNode> monthNodes = new ArrayList<>();
					for (Map.Entry<Integer, List<LocalDate>> monthIt : months.entrySet()) {
						int month = monthIt.getKey();
						DateNode monthNode = new MonthDateNode(year, month,
								new Message("archive.month", month + "月", month));
						Set<Integer> days = monthIt.getValue().stream()
								.collect(Collectors.groupingBy(LocalDate::getDayOfMonth)).keySet();
						List<DateNode> dayNodes = new ArrayList<>(days.size());
						for (Integer day : days) {
							DateNode dayNode = new DayDateNode(year, month, day,
									new Message("archive.day", day + "日", day));
							setArticles(dayNode, articles);
							dayNodes.add(dayNode);
						}
						Collections.sort(dayNodes);
						monthNode.addAll(dayNodes);
						monthNodes.add(monthNode);
					}
					Collections.sort(monthNodes);
					yearNode.addAll(monthNodes);
					nodes.add(yearNode);
				}
				break;
			}
			Collections.sort(nodes);
		} else {
			nodes = new ArrayList<>();
		}
	}

	private void setArticles(DateNode node, List<Article> articles) {
		List<Article> _articles = node.find(articles);
		_articles.sort(articleComparator);
		node.addAll(_articles);
		articles.removeAll(_articles);
	}

	private LocalDate getLocalDate(Article article) {
		return Times.toLocalDateTime(article.getPubDate()).toLocalDate();
	}

	public List<DateNode> getNodes() {
		return nodes;
	}

	public abstract class DateNode implements Comparable<DateNode> {
		protected final int order;
		private final Message text;
		private final List<Object> nodes = new ArrayList<>();

		private DateNode(int order, Message text) {
			super();
			this.order = order;
			this.text = text;
		}

		public Message getText() {
			return text;
		}

		public List<Object> getNodes() {
			return nodes;
		}

		public void addAll(List<?> objs) {
			this.nodes.addAll(objs);
		}

		public int getOrder() {
			return order;
		}

		@Override
		public int compareTo(DateNode o) {
			return -Integer.compare(order, o.order);
		}

		protected abstract List<Article> find(List<Article> articles);
	}

	private final class YearDateNode extends DateNode {

		private YearDateNode(int order, Message text) {
			super(order, text);
		}

		@Override
		protected List<Article> find(List<Article> articles) {
			return articles.stream().filter(art -> Times.toLocalDateTime(art.getPubDate()).getYear() == order)
					.collect(Collectors.toList());
		}

	}

	private final class MonthDateNode extends DateNode {

		private final int year;

		private MonthDateNode(int year, int order, Message text) {
			super(order, text);
			this.year = year;
		}

		@Override
		protected List<Article> find(List<Article> articles) {
			return articles.stream().filter(art -> {
				LocalDateTime time = Times.toLocalDateTime(art.getPubDate());
				return time.getYear() == year && time.getMonthValue() == order;
			}).collect(Collectors.toList());
		}

	}

	private final class DayDateNode extends DateNode {

		private final int year;
		private final int month;

		private DayDateNode(int year, int month, int order, Message text) {
			super(order, text);
			this.year = year;
			this.month = month;
		}

		@Override
		protected List<Article> find(List<Article> articles) {
			return articles.stream().filter(art -> {
				LocalDateTime time = Times.toLocalDateTime(art.getPubDate());
				return time.getYear() == year && time.getMonthValue() == month && time.getDayOfMonth() == order;
			}).collect(Collectors.toList());
		}
	}

}

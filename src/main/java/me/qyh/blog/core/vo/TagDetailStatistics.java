package me.qyh.blog.core.vo;
public class TagDetailStatistics extends TagStatistics {
		/**
		 * 标签总数，标签没有空间的概念，所以这个值只有在空间为空的时候才会有
		 */
		private Integer total;

		public Integer getTotal() {
			return total;
		}

		public void setTotal(Integer total) {
			this.total = total;
		}
	}

	

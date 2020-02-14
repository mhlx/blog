package me.qyh.blog.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Category {

	private Integer id;
	@NotBlank(message = "类别名称不能为空")
	@Size(max = 20, message = "类别名称不能超过20个字符")
	@Pattern(regexp = "^[A-Za-z0-9\u4E00-\u9FA5]+$", message = "标签名只能是中英文字符、数字")
	private String name;
	private LocalDateTime createTime;
	private LocalDateTime modifyTime;

	public Category() {
		super();
	}

	public Category(Integer id) {
		super();
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public LocalDateTime getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(LocalDateTime modifyTime) {
		this.modifyTime = modifyTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		return id != null && id.equals(other.id);
	}

}

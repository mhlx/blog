package me.qyh.blog.core.entity;

import java.sql.Timestamp;
import java.util.Objects;

import me.qyh.blog.core.util.Validators;

/**
 * 
 * @author Administrator
 *
 */
public class Space extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private String name;// 空间名
	private String alias;
	private Timestamp createDate;// 创建时间
	private Boolean isPrivate;

	private Boolean isDefault;
	private String lockId;

	/**
	 * default
	 */
	public Space() {
		super();
	}

	/**
	 * 
	 * @param id
	 *            空间id
	 */
	public Space(Integer id) {
		super(id);
	}

	/**
	 * 
	 * @param alias
	 *            空间alias
	 */
	public Space(String alias) {
		this.alias = alias;
	}

	/**
	 * clone
	 * 
	 * @param space
	 */
	public Space(Space space) {
		this.id = space.id;
		this.alias = space.alias;
		this.createDate = space.createDate;
		this.isDefault = space.isDefault;
		this.isPrivate = space.isPrivate;
		this.name = space.name;
		this.lockId = space.lockId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public boolean hasLock() {
		return lockId != null;
	}

	public String getLockId() {
		return lockId;
	}

	public void setLockId(String lockId) {
		this.lockId = lockId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			Space rhs = (Space) obj;
			return Objects.equals(this.id, rhs.id);
		}
		return false;
	}

}

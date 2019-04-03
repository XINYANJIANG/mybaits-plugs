package com.wtn.mybatis_plugs.util;

import java.util.List;

/**
   * 根据easyui的datagrid控件组织的Page对象
 * @author Think
 *
 * @param <T>
 */
public class PageResult<T> {

	private int pageNo = 1;// 页码，默认是第一页
	private int pageSize = 3;// 每页显示的记录数，默认是15
	private int total;// 总记录数
	private int totalPage;// 总页数
	private List<T> rows;

	private int offset = 0;// 偏移量，当前页起始记录数
	private int limit = 15;

	private boolean count = true;

	public PageResult() {
	}

	public PageResult(int pageNo, int pageSize, int total, List<T> rows) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.total = total;
		this.setRows(rows);
		int totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		this.setTotalPage(totalPage);
	}

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
//		this.offset = offset;
		this.offset = (this.pageNo - 1) * pageSize;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isCount() {
		return count;
	}

	public void setCount(boolean count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "Page [pageNo=" + pageNo + ", pageSize=" + pageSize + ", totalRecord=" + total + ", totalPage="
				+ totalPage + ", rows=" + rows + ", offset=" + offset + ", limit=" + limit + ", count=" + count + "]";
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		// 在设置总页数的时候计算出对应的总页数，在下面的三目运算中加法拥有更高的优先级，所以最后可以不加括号。
		int totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		this.setTotalPage(totalPage);
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

}

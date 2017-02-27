package com.wotingfm.ui.interphone.group.creategroup.model;


import java.io.Serializable;

public class Freq implements Serializable{

	private String CatalogType;
	private String CatalogBCode;
	private String CatalogName;
	private String CatalogAliasName;
	private String CatalogId;


	public String getCatalogType() {
		return CatalogType;
	}

	public void setCatalogType(String catalogType) {
		CatalogType = catalogType;
	}

	public String getCatalogBCode() {
		return CatalogBCode;
	}

	public void setCatalogBCode(String catalogBCode) {
		CatalogBCode = catalogBCode;
	}

	public String getCatalogName() {
		return CatalogName;
	}

	public void setCatalogName(String catalogName) {
		CatalogName = catalogName;
	}

	public String getCatalogAliasName() {
		return CatalogAliasName;
	}

	public void setCatalogAliasName(String catalogAliasName) {
		CatalogAliasName = catalogAliasName;
	}

	public String getCatalogId() {
		return CatalogId;
	}

	public void setCatalogId(String catalogId) {
		CatalogId = catalogId;
	}
}

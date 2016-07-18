package com.ml.logistica.romaneio.entity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class PackingListType {
	private Long id;
	private String name;

	private static List<PackingListType> instances;

	public static PackingListType Traditional;
	public static PackingListType Courrier;

	static {
		Traditional = new PackingListType(1l, "Traditional");
		Courrier = new PackingListType(2l, "Courrier");
		instances = Arrays.asList(new PackingListType[] { Traditional, Courrier });
	}

	private PackingListType(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private static PackingListType getInstance(Predicate<PackingListType> predicate) {
		List<PackingListType> l = instances.stream().filter(predicate).collect(Collectors.toList());
		if (l != null && l.size() > 0) {
			return l.get(0);
		}
		return null;
	}

	public static PackingListType valueOf(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		return getInstance(p -> {
			return name.equals(p.getName());
		});
	}

	public static PackingListType valueOf(Long id) {
		if (id == null || id.longValue() <= 0) {
			return null;
		}
		return getInstance(p -> {
			return id.longValue() == p.getId().longValue();
		});
	}

}

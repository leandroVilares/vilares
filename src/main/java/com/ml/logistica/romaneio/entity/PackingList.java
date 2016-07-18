package com.ml.logistica.romaneio.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PackingList {

	/**
	 * Número do romaneio
	 */
	private Long id;
	/**
	 * Filial
	 */
	private Branch branch;
	/**
	 * Pedido, Tradicional
	 */
	private PackingListType type;
	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Calendar createdAt;
	/**
	 * Id veículo usado no faturamento (GEMCO)
	 */
	private Long truck;
	/**
	 * Placa do Luiza GOL - Placa do Veículo o qual efetivemente saiu com o
	 * romaneio.
	 */
	private String deliveryTruckPlate;
	/**
	 * Pedidos e lotes.
	 */
	private List<PackingListDetail> details = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public PackingListType getType() {
		return type;
	}

	public void setType(PackingListType type) {
		this.type = type;
	}

	public Calendar getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Calendar date) {
		this.createdAt = date;
	}

	public List<PackingListDetail> getDetails() {
		return details;
	}

	public void setDetails(List<PackingListDetail> details) {
		this.details = details;
	}

	public Long getTruck() {
		return truck;
	}

	public void setTruck(Long orderTruck) {
		this.truck = orderTruck;
	}

	public String getDeliveryTruckPlate() {
		return deliveryTruckPlate;
	}

	public void setDeliveryTruckPlate(String deliveryTruckPlate) {
		this.deliveryTruckPlate = deliveryTruckPlate;
	}

}

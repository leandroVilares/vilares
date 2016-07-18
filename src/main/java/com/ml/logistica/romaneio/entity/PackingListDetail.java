package com.ml.logistica.romaneio.entity;

public class PackingListDetail {
	private Order order; // Id Pedido
	private Long batch; // Id Lote pedido

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Long getBatch() {
		return batch;
	}

	public void setBatch(Long idLotePedido) {
		this.batch = idLotePedido;
	}

}

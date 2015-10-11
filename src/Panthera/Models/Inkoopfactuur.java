package Panthera.Models;

import java.sql.Date;

public class Inkoopfactuur {
	private int id;
	private int factuurnummer;
	private Date factuurdatum;
	private Date vervaldatum;
	
	public Inkoopfactuur() {
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getFactuurnummer() {
		return factuurnummer;
	}
	public void setFactuurnummer(int factuurnummer) {
		this.factuurnummer = factuurnummer;
	}
	public Date getFactuurdatum() {
		return factuurdatum;
	}
	public void setFactuurdatum(Date factuurdatum) {
		this.factuurdatum = factuurdatum;
	}
	public Date getVervaldatum() {
		return vervaldatum;
	}
	public void setVervaldatum(Date vervaldatum) {
		this.vervaldatum = vervaldatum;
	}
}

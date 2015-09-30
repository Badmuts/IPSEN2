package Panthera.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Panthera.Models.Bestellijst;
import Panthera.Models.Product;

/**
 * 
 * @author Roy
 *
 */
public class BestellijstDAO extends DAO {

	public BestellijstDAO() throws IllegalAccessException, InstantiationException, SQLException {
		super();
		// TODO Auto-generated constructor stub
	}

	public Bestellijst get(int id) throws Exception {
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("SELECT * FROM bestellijst b, product_to_bestellijst pb, product p WHERE b.id=" + id + " AND pb.product_id=p.id");
		Bestellijst bestellijst = new Bestellijst();
		while(result.next()) {
			bestellijst.setId(result.getInt("id"));
			bestellijst.setDate(result.getDate("date"));
            bestellijst.setName(result.getString("name"));
			bestellijst.addProduct(new Product(
					result.getInt("id"),
					result.getInt("productnummer"),
					result.getString("naam"),
					result.getInt("jaar"),
					result.getDouble("prijs"),
					result.getString("type"),
					new LandDAO().get(result.getInt("land_id"))));
		}
		return bestellijst;
	}

	public ArrayList<Bestellijst> all() throws Exception {
		ArrayList<Bestellijst> bestellijsten = new ArrayList<>();
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("SELECT b.id AS bid, b.date, b.name, p.id AS pid, p.productnummer, p.naam, p.jaar, p.prijs, p.type, p.land_id FROM bestellijst b, product_to_bestellijst pb, product p WHERE pb.bestellijst_id=b.id AND pb.product_id=p.id");
        int lastId = 0;
        Bestellijst bestellijst = new Bestellijst();
		while(result.next()) {

            // Als het laatste id niet gelijk is aan het huidige id
            // leeg dan het bestellijst object.
            // Anders, blijf het bestellijst object vullen met producten.
            if (lastId != result.getInt("bid") && lastId != 0) {
                bestellijsten.add(bestellijst);
                bestellijst = new Bestellijst();
            }

            bestellijst.setId(result.getInt("bid"));
            bestellijst.setDate(result.getDate("date"));
            bestellijst.setName(result.getString("name"));
            bestellijst.addProduct(new Product(
                    result.getInt("pid"),
                    result.getInt("productnummer"),
                    result.getString("naam"),
                    result.getInt("jaar"),
                    result.getDouble("prijs"),
                    result.getString("type"),
                    new LandDAO().get(result.getInt("land_id"))));
            lastId = result.getInt("bid");

		}
        bestellijsten.add(bestellijst);
		return bestellijsten;
	}

	public void saveNewBestellijst(List<Product> producten) {
		try {
			//Get the new bestellijst_id.
			int bestellijst_id = getNewBestellijstId();
			
			//Now add each product to this bestellijst_id in bestellijst table.
			insertBestellijstRecords(bestellijst_id, producten);
			
		} catch(SQLException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			System.out.println("Wrong bestellijst id.");
			ex.printStackTrace();
		} 
	}
	
	/**
	 * Insert each product in bestellijst table with given bestellijst_id.
	 * @param bestellijst_id
	 * @param producten
	 * @throws SQLException 
	 */
	public void insertBestellijstRecords(int bestellijst_id, List<Product> producten) throws SQLException {
		Statement stmt = conn.createStatement();
		for(Product product : producten) {
			String query = (
					"INSERT INTO bestellijst(bestellijst_id, product_id)" +
					"VALUES(" +
					bestellijst_id + ", " +
					product.getId() +
					")");
			stmt.executeUpdate(query);
		}
	}
	
	public int getNewBestellijstId() throws SQLException, Exception {
		int id = 1;
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("SELECT MAX(bestellijst_id) AS id FROM bestellijst");
		while(result.next()) {
			id = result.getInt("id");
		}
		//id never is 0, means it's not set.
		if(id < 0) {
			throw new Exception();
		}
		//New bestellijst_id should be one higher than the current max id.
		id += 1;
		return id;
	}

}

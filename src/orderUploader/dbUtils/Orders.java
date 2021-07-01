package orderUploader.dbUtils;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class Orders extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
	
	protected int idOrder;
	protected int idCustomer;
	protected int idCustomerDelivery;
	protected String customerOrderRef;
	protected String status;
	protected Date requestedAssemblyDate;
	protected Date effectiveAssemblyDate;
	protected Date shipmentDate;
	protected int numberOfItemsToShip;
    protected int palletLength;
    protected int palletWidth;
    protected int palletHeigth;
    protected int palletWeigth;
    protected String orderRef;
    protected String transportDocNum;
    protected String forwarder;
    protected double forwarderCost;
    protected double clientCost;
    protected int assemblyTimeAuto;
    protected int assemblyTime;
    protected int palletCost;
    protected int insuranceCost;
    protected String customerRefERP;
    protected String customerDescription;
    protected String customerDeliveryProvince;
    protected int compositionBoards;
    protected int compositionTrays;
    protected int compositionDesign;
    protected int compositionAccessories;
    protected String sourceIssue;
    protected double orderValue;
    protected String confirmationEmail;
    protected double invoiceValue;
	protected boolean selected = false;
	
	private void setNames()
	{
		tableName = "Orders";
		idColName = "idOrder";
	}

	public Orders()
	{
		setNames();
	}

	public Orders(DBConnection conn, int id) throws Exception
	{
		getOrders(conn, id);
	}

	public void getOrders(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description AS customerDescription, b.refERP AS customerRefERP, " +
				 "       c.province AS customerDeliveryProvince " +
				 "FROM Orders a INNER JOIN Customers b ON " +
				 "     (a.idCustomer = b.idCustomers) " +
				 "   INNER JOIN CustomerDelivery c ON " +
				 "     (c.idCustomerDelivery = a.idCustomerDelivery) " +
				 "WHERE idOrder = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Orders> getOrders(DBConnection conn, String whereCondition, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Orders.class);
		String sql = "SELECT a.*, b.description AS customerDescription, b.refERP AS customerRefERP, " +
				 "       c.province AS customerDeliveryProvince " +
				 "FROM Orders a INNER JOIN Customers b ON " +
				 "     (a.idCustomer = b.idCustomers) " +
				 "   INNER JOIN CustomerDelivery c ON " +
				 "     (c.idCustomerDelivery = a.idCustomerDelivery) " +
				 "WHERE " + whereCondition;
		log.trace("Querying: " + sql);
		return (ArrayList<Orders>) DBInterface.populateCollection(conn, sql, Orders.class);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Orders> getOrdersActive(DBConnection conn, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Orders.class);
		String sql = "SELECT a.*, b.description AS customerDescription, b.refERP AS customerRefERP, " +
					 "       c.province AS customerDeliveryProvince " +
					 "FROM Orders a INNER JOIN Customers b ON " +
					 "     (a.idCustomer = b.idCustomers) " +
					 "   INNER JOIN CustomerDelivery c ON " +
					 "     (c.idCustomerDelivery = a.idCustomerDelivery) " +
					 "WHERE status != \"SHI\"";
		log.trace("Querying: " + sql);
		return (ArrayList<Orders>) DBInterface.populateCollection(conn, sql, Orders.class);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Orders> getOrdersByStatusSet(DBConnection conn, String statusSet, int languageCode) throws Exception {
		Logger log = Logger.getLogger(Orders.class);
		String sql = "SELECT a.*, b.description AS customerDescription, b.refERP AS customerRefERP, " +
					 "       c.province AS customerDeliveryProvince " +
					 "FROM Orders a INNER JOIN Customers b ON " +
					 "     (a.idCustomer = b.idCustomers) " +
					 "   INNER JOIN CustomerDelivery c ON " +
					 "     (c.idCustomerDelivery = a.idCustomerDelivery) " +
					 "WHERE status IN (" + statusSet + ")";
		log.trace("Querying: " + sql);
		return (ArrayList<Orders>) DBInterface.populateCollection(conn, sql, Orders.class);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Articles> getOrderArticles(DBConnection conn, int id) throws Exception {
		Logger log = Logger.getLogger(Orders.class);
		String sql = "SELECT b.* " +
					 "FROM OrderDetails a INNER JOIN Articles b ON " +
					 "     (a.idArticle = b.idArticle) " +
					 "WHERE a.idOrder = " + id;
		log.trace("Querying: " + sql);
		return (ArrayList<Articles>) DBInterface.populateCollection(conn, sql, Articles.class);
	}

	public void getOrderFromRefERP(DBConnection conn, String orderRef, int languageCode) throws Exception 
	{
		Logger log = Logger.getLogger(Orders.class);
		String sql = "SELECT * " +
					 "FROM Orders " +
					 "WHERE orderRef ='" + orderRef + "'";
		log.trace("Querying: " + sql);
		populateObject(conn, sql, this);
	}

	public int getIdOrder() {
		return idOrder;
	}

	public void setIdOrder(int idOrder) {
		this.idOrder = idOrder;
	}

	public int getIdCustomer() {
		return idCustomer;
	}

	public void setIdCustomer(int idCustomer) {
		this.idCustomer = idCustomer;
	}

	public int getIdCustomerDelivery() {
		return idCustomerDelivery;
	}

	public void setIdCustomerDelivery(int idCustomerDelivery) {
		this.idCustomerDelivery = idCustomerDelivery;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getRequestedAssemblyDate() {
		return requestedAssemblyDate;
	}

	public void setRequestedAssemblyDate(Date requestedAssemblyDate) {
		this.requestedAssemblyDate = requestedAssemblyDate;
	}

	public Date getEffectiveAssemblyDate() {
		return effectiveAssemblyDate;
	}

	public void setEffectiveAssemblyDate(Date effectiveAssemblyDate) {
		this.effectiveAssemblyDate = effectiveAssemblyDate;
	}

	public Date getShipmentDate() {
		return shipmentDate;
	}

	public void setShipmentDate(Date shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	public int getPalletLength() {
		return palletLength;
	}

	public void setPalletLength(int palletLength) {
		this.palletLength = palletLength;
	}

	public int getPalletWidth() {
		return palletWidth;
	}

	public void setPalletWidth(int palletWidth) {
		this.palletWidth = palletWidth;
	}

	public int getPalletHeigth() {
		return palletHeigth;
	}

	public void setPalletHeigth(int palletHeigth) {
		this.palletHeigth = palletHeigth;
	}

	public int getPalletWeigth() {
		return palletWeigth;
	}

	public void setPalletWeigth(int palletWeigth) {
		this.palletWeigth = palletWeigth;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}

	public String getTransportDocNum() {
		return transportDocNum;
	}

	public void setTransportDocNum(String transportDocNum) {
		this.transportDocNum = transportDocNum;
	}

	public String getForwarder() {
		return forwarder;
	}

	public void setForwarder(String forwarder) {
		this.forwarder = forwarder;
	}

	public double getForwarderCost() {
		return forwarderCost;
	}

	public void setForwarderCost(double forwarderCost) {
		this.forwarderCost = forwarderCost;
	}

	public double getClientCost() {
		return clientCost;
	}

	public void setClientCost(double clientCost) {
		this.clientCost = clientCost;
	}

	public int getAssemblyTimeAuto() {
		return assemblyTimeAuto;
	}

	public void setAssemblyTimeAuto(int assemblyTimeAuto) {
		this.assemblyTimeAuto = assemblyTimeAuto;
	}

	public int getAssemblyTime() {
		return assemblyTime;
	}

	public void setAssemblyTime(int assemblyTime) {
		this.assemblyTime = assemblyTime;
	}

	public int getPalletCost() {
		return palletCost;
	}

	public void setPalletCost(int palletCost) {
		this.palletCost = palletCost;
	}

	public int getInsuranceCost() {
		return insuranceCost;
	}

	public void setInsuranceCost(int insuranceCost) {
		this.insuranceCost = insuranceCost;
	}

	public String getCustomerDescription() {
		return customerDescription;
	}

	public void setCustomerDescription(String customerDescription) {
		this.customerDescription = customerDescription;
	}

	public String getCustomerRefERP() {
		return customerRefERP;
	}

	public void setCustomerRefERP(String customerRefERP) {
		this.customerRefERP = customerRefERP;
	}

	public String getCustomerDeliveryProvince() {
		return customerDeliveryProvince;
	}

	public void setCustomerDeliveryProvince(String customerDeliveryProvince) {
		this.customerDeliveryProvince = customerDeliveryProvince;
	}

	public int getNumberOfItemsToShip() {
		return numberOfItemsToShip;
	}

	public void setNumberOfItemsToShip(int numberOfItemsToShip) {
		this.numberOfItemsToShip = numberOfItemsToShip;
	}

	public int getCompositionBoards() {
		return compositionBoards;
	}

	public void setCompositionBoards(int compositionBoards) {
		this.compositionBoards = compositionBoards;
	}

	public int getCompositionTrays() {
		return compositionTrays;
	}

	public void setCompositionTrays(int compositionTrays) {
		this.compositionTrays = compositionTrays;
	}

	public int getCompositionDesign() {
		return compositionDesign;
	}

	public void setCompositionDesign(int compositionDesign) {
		this.compositionDesign = compositionDesign;
	}

	public int getCompositionAccessories() {
		return compositionAccessories;
	}

	public void setCompositionAccessories(int compositionAccessories) {
		this.compositionAccessories = compositionAccessories;
	}

	public double getOrderValue() {
		return orderValue;
	}

	public void setOrderValue(double orderValue) {
		this.orderValue = orderValue;
	}

	public String getSourceIssue() {
		return sourceIssue;
	}

	public void setSourceIssue(String sourceIssue) {
		this.sourceIssue = sourceIssue;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getCustomerOrderRef() {
		return customerOrderRef;
	}

	public void setCustomerOrderRef(String customerOrderRef) {
		this.customerOrderRef = customerOrderRef;
	}

	public String getConfirmationEmail() {
		return confirmationEmail;
	}

	public void setConfirmationEmail(String confirmationEmail) {
		this.confirmationEmail = confirmationEmail;
	}

	public double getInvoiceValue() {
		return invoiceValue;
	}

	public void setInvoiceValue(double invoiceValue) {
		this.invoiceValue = invoiceValue;
	}
}

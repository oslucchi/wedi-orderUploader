package orderUploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import orderUploader.dbUtils.Articles;
import orderUploader.dbUtils.CustomerDelivery;
import orderUploader.dbUtils.Customers;
import orderUploader.dbUtils.DBConnection;
import orderUploader.dbUtils.DBInterface;
import orderUploader.dbUtils.OrderDetails;
import orderUploader.dbUtils.OrderNotes;
import orderUploader.dbUtils.OrderShipment;
import orderUploader.dbUtils.Orders;
import orderUploader.utils.ApplicationProperties;
import orderUploader.utils.Utils;

public class PDFReader {
	private static DBConnection conn;
	private static CustomerDelivery cd;
	final static Logger log = Logger.getLogger(PDFReader.class);
	static int components[] = { 0, 0, 0, 0 };
	static double orderValue = 0;
	static boolean sourceIssue = false;
	
	public PDFReader()
	{
		
	}

	private static void getDeliveryData(String text, Customers customer)
	{
		int offset;
		String searchFor;
		List<String> address = new ArrayList<String>();
				
		cd = new CustomerDelivery();
		cd.setIdCustomer(customer.getIdCustomers());

		searchFor = "Indirizzo di consegna:";
		if ((offset = text.indexOf(searchFor)) == -1)
		{
			searchFor = "stedoqST_Representative2 (it) nicht gefunden!@@";
			offset = text.indexOf(searchFor);
		}
		offset += searchFor.length() + 1;
		while(!text.substring(offset).startsWith("Conferma d'ordine"))
		{
			address.add(text.substring(offset , text.indexOf("\n", offset + 1)));
			offset = text.indexOf("\n", offset + 1) + 1;			
		}
		log.debug("Found addresses:");
		int i;
		for(i = 0; i < address.size(); i++)
		{
			log.debug(address.get(i));
		}
		i = address.size() - 1;
		offset = address.get(i).indexOf(" ");
		if (offset >= 0)
		{
			cd.setZipCode(address.get(i).substring(0, offset));
		}
		else
		{
			cd.setZipCode("00000");
		}
		if (cd.getZipCode().length() > 5)
		{
			log.error("Il CAP del cliente " + customer.getRefERP() + " " + customer.getDescription() +
					  " e' errato (" + cd.getZipCode() + ")");
			cd.setZipCode(cd.getZipCode().substring(cd.getZipCode().length() - 5));
		}
		offset++;
		int lastSpace = address.get(i).length() - 2;
		cd.setProvince(address.get(i).substring(lastSpace));
		if (lastSpace > offset)
		{
			cd.setCity(address.get(i).substring(offset, lastSpace - 1));
		}
		else
		{
			cd.setCity("XXXX");
		}
		i--;
		cd.setAddress(address.get(i));
		
		if (--i > 0)
		{
			cd.setNotes(address.get(i));
			i--;
		}
		if (cd.getProvince().length() > 2)
		{
			cd.setProvince("XX");
		}
		log.debug("CAP: " + cd.getZipCode() + " - CITTA': " + cd.getCity() + " - PV: " + cd.getProvince() + 
				  ("ADDRESS: " + cd.getAddress()));
		try
		{
			DBInterface.TransactionStart(conn);
			ArrayList<CustomerDelivery> cdTemp = CustomerDelivery
					.getCustomerDeliveryByAddress(conn, customer.getIdCustomers(), cd.getAddress(), 1);
			if ((cdTemp == null) || (cdTemp.size() == 0))
			{
				int idInsert = cd.insertAndReturnId(conn, "idCustomerDelivery", cd);
				cd.setIdCustomerDelivery(idInsert);
			}
			else
			{
				if ((cdTemp.get(0).getProvince() == null || cdTemp.get(0).getProvince().compareTo("") == 0))
				{
					cdTemp.get(0).setProvince(cd.getProvince());
					cdTemp.get(0).update(conn, "idCustomerDelivery");
				}
				cd.setIdCustomerDelivery(cdTemp.get(0).getIdCustomerDelivery());
			}
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e)
		{
			DBInterface.TransactionRollback(conn);
			e.printStackTrace();
			log.error(e);
		}
	}

	private static int getArticleFromDB(String refERP) throws Exception
	{
		Articles article;
		try
		{
			article = Articles.getArticleByRefERP(conn, refERP);
			return(article.getIdArticle());
		}
		catch(Exception e1)
		{
			if (e1.getMessage().compareTo("No record found") != 0)
			{
				throw e1;
			}
			Articles ar = new Articles();
			ar.setRefERP(refERP);
			ar.setDescription("*** ARTICOLO NON TROVATO ***");
			ar.setRateOfConversion(1.0);
			ar.setBuyPrice(0);
			ar.setGrossPrice(0);
			ar.setCategory("*");
			ar.setHeigth(0);
			ar.setLength(0);
			ar.setWidth(0);
			ar.setWeigth(0);
			ar.setIdArticle(0);
			ar.setUnityOfMeasure("*");
			try
			{
				ar.setIdArticle(ar.insertAndReturnId(conn, "idArticle", ar));
			}
			catch(Exception e2)
			{
				throw e1;
			}
			return(ar.getIdArticle());
		}
	}
	
	private static boolean isNumeric(String strNum) 
	{
		strNum = strNum.replaceAll("\\,", ".");
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        Double.parseDouble(strNum);
	    } 
	    catch (NumberFormatException nfe) 
	    {
	        return false;
	    }
	    return true;
	}
	
	private static void evaluateArticleDetails(String[] articleDetails, OrderDetails od, int idOrder) throws Exception
	{
		Articles article = null;
		String value;
		
		if (articleDetails.length < 4)
		{
			log.error("riga articolo speciale.... ");
			value = articleDetails[1].substring(articleDetails[1].length() - 11, articleDetails[1].length() - 2);
			od.setIdOrder(idOrder);
			od.setIdArticle(getArticleFromDB(value));
			od.setQuantity(Double.parseDouble(articleDetails[0]));
			article = new Articles(conn, od.getIdArticle());
			orderValue += od.getQuantity() * article.getBuyPrice();
			od.setCost(article.getBuyPrice());
			switch(article.getCategory())
			{
            case "A":
              components[0]++;
              break;
            case "BS":
              components[1] += (int)(od.getQuantity() / article.getRateOfConversion());
              break;
            case "D":
              components[2]++;
              break;
            case "T":
              components[3]++;
              break;
			}
			od.setSourceIssue(1);
			sourceIssue = true;
		}
		else
		{
			value = articleDetails[3].substring(articleDetails[3].length() - 11, articleDetails[3].length() - 2);
			od.setIdOrder(idOrder);
			od.setIdArticle(getArticleFromDB(value));
			od.setQuantity(Double.parseDouble(articleDetails[0]));
			article = new Articles(conn, od.getIdArticle());
			orderValue += od.getQuantity() * article.getBuyPrice();
			od.setCost(article.getBuyPrice());
			switch(article.getCategory())
			{
            case "A":
              components[0]++;
              break;
            case "BS":
              components[1] += (int)(od.getQuantity() / article.getRateOfConversion());
              break;
            case "D":
              components[2]++;
              break;
            case "T":
              components[3]++;
              break;
			}
			String um = articleDetails[3].substring(articleDetails[3].length() - 2);
			if (articleDetails[4].length() == um.length()) 
			{
				if (um.compareTo(articleDetails[4].substring(0, um.length())) != 0)
				{
					od.setSourceIssue(1);
					sourceIssue = true;
				}
				else
				{
					od.setSourceIssue(0);
				}
			}
			else
			{
				od.setSourceIssue(1);
				sourceIssue = true;
			}
			if (article.getRateOfConversion() != 0)
			{
				od.setPiecesFromSqm((int)(od.getQuantity()/article.getRateOfConversion()));
			}
		}
	}
	
	public static void getDataFromPDF(InputStream input, ApplicationProperties ap, String mailTo) throws Exception
	{
		int offset = 0;
		int idInsert = 0;
		boolean updateOrder = false;
		
		for(int i = 0; i < components.length; i++)
		{
			components[i] = 0;
		}
		sourceIssue = false;
		orderValue = 0;
				
		ArrayList<OrderDetails> orderDetails = new ArrayList<OrderDetails>();
		OrderDetails od;
//		Articles article = new Articles();
		Orders order = new Orders();
		String[] articleDetails;
		PDDocument document;
		String text = "";

		try 
		{
			document = PDDocument.load(input);
			PDFTextStripper pdfStripper = new PDFTextStripper();
			text = pdfStripper.getText(document);
			log.debug(text);
			document.close();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.error(e1);
			throw e1;
		}

		try
		{
			conn = DBInterface.connect(ap);
			DBInterface.TransactionStart(conn);
			String searchFor = "stedoqST_OrderNo (it) nicht gefunden!";
			offset = text.indexOf(searchFor) + searchFor.length();
			String value = text.substring(offset , offset + 8);
			order.setOrderRef(value);
			try
			{
				order.getOrderFromRefERP(conn, value, 1);
				log.debug("order already in the system");
				updateOrder = true;
				try
				{
					OrderDetails.deleteOrderDetails(conn, order.getIdOrder(), 1);
					OrderNotes.deleteOrderNotes(conn, order.getIdOrder(), 1);
					OrderShipment.deleteOrderShipment(conn, order.getIdOrder(), 1);
				}
				catch(Exception e2)
				{
					;
				}
			}
			catch(Exception e1)
			{
				if (e1.getMessage().compareTo("No record found") != 0)
				{
					throw e1;
				}
			}

			searchFor = "stedoqST_CustomerNo (it) nicht gefunden!";
			offset = text.indexOf(searchFor) + searchFor.length();
			value = text.substring(offset , offset + 8);
			log.debug("Codice cliente: " + value);
			Customers customer = null;
			try
			{
				customer = Customers.getCustomersbyERPRef(conn, value);
			}
			catch(Exception ec)
			{
				if (ec.getMessage().compareTo("No record found") == 0)
				{
					customer = new Customers();
					customer.setIdCustomers(0);
					customer.setRefERP(value);
					searchFor = "stedoqST_Representative2 (it) nicht gefunden!@@";
					offset = text.indexOf(searchFor) + searchFor.length() + 1;
					customer.setDescription(text.substring(offset, text.indexOf("\n", offset)));
					customer.setIdCustomers(customer.insertAndReturnId(conn, "idCustomers", customer));
				}
				else
				{
					throw ec;
				}
			}
			order.setConfirmationEmail(mailTo);
			order.setIdCustomer(customer.getIdCustomers());
			order.setStatus("SYS");

			searchFor = "Data di consegna:";
			offset = text.indexOf(searchFor) + searchFor.length() + 1;
			value = text.substring(offset , text.substring(offset).indexOf("\n") + offset);
			order.setCustomerOrderRef(value);
			
			searchFor = "circa ";
			offset = text.indexOf(searchFor) + searchFor.length();
			value = text.substring(offset , offset + 10);
			value = value.replaceAll("\\.", "/");
			order.setRequestedAssemblyDate(new SimpleDateFormat("dd/MM/yyyy").parse(value));
			
			getDeliveryData(text, customer);
			order.setIdCustomerDelivery(cd.getIdCustomerDelivery());
			if (updateOrder)
			{
				order.update(conn, "idOrder");
			}
			else
			{
				order.setIdOrder(0);
				idInsert = order.insertAndReturnId(conn, "idOrder", order);
				order.setIdOrder(idInsert);
				OrderShipment os = new OrderShipment();
				os.setIdOrder(idInsert);
				os.insert(conn, "idOrderShipment", os);
			}
			DBInterface.TransactionCommit(conn);

			log.debug("Numero Ordine: " + order.getOrderRef());					
			while((offset = text.indexOf("PZ ", offset + 1)) > 0)
			{
				log.debug(text.substring(offset, offset + 10));
				if (text.charAt(offset + 3) == '=')
				{
					offset += 5;
					continue;
				}				
				String temp =  text.substring(1, text.substring(offset).indexOf("\n") + offset - 1);
				value = text.substring(temp.lastIndexOf("\n") + 2, temp.lastIndexOf("\n") + 6);
				if (!isNumeric(value))
					continue;

				for(; (int) text.charAt(offset - 1) != 10; offset--)
					;
				value = text.substring(offset, text.substring(offset).indexOf("\n") + offset - 1);
				value = value.substring(0, value.lastIndexOf("PZ") + 2);
				log.debug("Dati articolo: " + value);
				articleDetails = Utils.removeEmptyEntries(value.trim().split(" "));
				od = new OrderDetails();
				evaluateArticleDetails(articleDetails, od, order.getIdOrder());
				orderDetails.add(od);
				offset += text.substring(offset).indexOf("\n") + 1;
			}
			offset = 100;
			while((offset = text.indexOf("mq ", offset + 1)) > 0)
			{
				String temp =  text.substring(1, text.substring(offset).indexOf("\n") + offset - 1);
				value = text.substring(temp.lastIndexOf("\n") + 2, temp.lastIndexOf("\n") + 6);
				if (!isNumeric(value))
					continue;

				for(; (int) text.charAt(offset - 1) != 10; offset--)
					;
				value = text.substring(offset, text.substring(offset).indexOf("\n") + offset - 1);
				value = value.substring(0, value.lastIndexOf("mq") + 2);
				log.debug("Dati articolo: " + value);
				articleDetails = Utils.removeEmptyEntries(value.trim().split(" "));
				od = new OrderDetails();
				evaluateArticleDetails(articleDetails, od, order.getIdOrder());
				orderDetails.add(od);
				offset += text.substring(offset).indexOf("\n") + 1;
			}
			offset = 100;
			while((offset = text.indexOf("m ", offset + 1)) > 0)
			{
				String temp =  text.substring(1, text.substring(offset).indexOf("\n") + offset - 1);
				value = text.substring(temp.lastIndexOf("\n") + 2, temp.lastIndexOf("\n") + 6);
				if (!isNumeric(value))
					continue;
				
				for(; (int) text.charAt(offset - 1) != 10; offset--)
					;
				value = text.substring(offset, text.substring(offset).indexOf("\n") + offset - 1);
				value = value.substring(0, value.lastIndexOf(" m") + 2);
				log.debug("Dati articolo: " + value);
				articleDetails = Utils.removeEmptyEntries(value.trim().split(" "));
				od = new OrderDetails();
				evaluateArticleDetails(articleDetails, od, order.getIdOrder());
				orderDetails.add(od);
				offset += text.substring(offset).indexOf("\n") + 1;
			}

			OrderDetails.insertCollection(conn, orderDetails, "idOrderDetails", OrderDetails.class);
			order.setCompositionBoards(components[1]);
			order.setCompositionTrays(components[3]);
			order.setCompositionDesign(components[2]);
			order.setCompositionAccessories(components[0]);
			order.setOrderValue(orderValue);
			if (sourceIssue)
			{
				order.setSourceIssue("X");
			}
			else
			{
				order.setSourceIssue("");
			}
			order.update(conn, "idOrder");
			log.debug("Composition and value: " + 
					  order.getCompositionBoards() + " " +
					  order.getCompositionTrays() + " " +
					  order.getCompositionDesign() + " " +
					  order.getCompositionAccessories() + " " +
					  order.getOrderValue());
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.error(e1);
			DBInterface.TransactionRollback(conn);
			throw e1;
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}
	
	public static void readFile(ApplicationProperties ap)
	{
		
		try (Stream<Path> walk = Files.walk(Paths.get("/archive/Dev/Projects/wedi/wedi-orderUploader/spool")))
		{

			List<String> result = walk.map(x -> x.toString())
									  .filter(p -> p.endsWith(".pdf"))
									  .collect(Collectors.toList());
			result.forEach(p -> {
				log.debug("working on " + p);
				//Loading an existing document
				File file = new File(p);
				try {
					getDataFromPDF(new FileInputStream(file), ap, "");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					log.error(e);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e);
				}
			});
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			log.error(e1);
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error(e1);
		}
	}
}

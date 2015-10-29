import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.instrument.ClassDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;



import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;



//IDEA 1------PLEASE IGNORE---------
	//to generate path:
	//generate a random target node x needed to reach
	//then find randomly choose one path from node 0 to node x
	//then find a path to go back from node x to node 0;

	//if speed considered, speed can be a random number, for example from 0.1 to 1
	
	//False Negative: once the real path sequence is generated, we can deleted lets say 5% records as radar can read fail
	
	//?at last may write result into a file?
	//--------------------------------------
			
			
			
//IDEA II -----currently using:---------
	//Generate items need to buy: ItemList (random number in a range)
	//for(item:itemList)    
	//	List.add(   findPath(currentArea,findArea(item))  ); 
	//                  findPath^--->BFS find the least deep leaf can be reached 
	//										--> Assumption: distance between each node is the same
	//List.add(   findPath(currentArea, Entry) )//entry maybe Radar[0];
	//radar ---> timeReserved ?
	//write numRecord,itemList,Goods-->output.xls 
	//--------------------------------------


public class entry {

	static int numRadar = 10;     //10 Radars
	static RadarNode[] node = new RadarNode[numRadar]; 
	static int numSellingArea = 10;
	static sellingArea[] sellAreasNode = new sellingArea[numSellingArea];

	public static void main(String[] args) throws IOException {
		
		String goodsList = "../JavaRFIDStoreSimulator/src/file/goodsList.csv";
		
		initialRadarSetting(numRadar, node);
		initialSellingAreaSetting(numSellingArea,sellAreasNode,node);
		purchaseList allGoods =  initialGoodsInitial(goodsList, sellAreasNode);
		
		allGoods.avePurGoods = 5;  //can be changed;
		int numCustomer = 1;    //number of buying lists	
		
		ArrayList<ArrayList<goods>> bigListForAllPurchasingRecords = new ArrayList<ArrayList<goods>>();
		//saving all single customer recordsinto a big array
		
		//this big for loop is for display all the 
		//purchasing behaviours records it has been generated
		for(int i=0;i<numCustomer;i++){
			ArrayList<goods> curList = allGoods.generatePurList();//single customer puchasing records
			for(goods g: curList){
				//System.out.print("itemName: "+g.itemName+" ");
				System.out.print("goodsId: "+g.goodsId+" ");
				System.out.print("percentage: "+g.percentage+" ");
				System.out.println("inArea: "+g.getInArea().areaId+" ");
			}
			
			ArrayList<area> areaList = goodsListToAreaList(curList);
			ArrayList<area> pathList = findPath(areaList);
			
			System.out.print("pathList: ");
			printAreaList(pathList);
			System.out.println();
			bigListForAllPurchasingRecords.add(curList);
			//print and write path --> excelfile "output.xls"
			//******************************
		}
		
		System.out.println("program end");	
		
	}
	
	//set radar connection
	public static void initialRadarSetting(int numNode, RadarNode[] node)
	{
		
		for(int i =0;i<numNode;i++)
		{
			node[i] = new RadarNode();
			String id = "R"+i;  //Radar
			node[i].areaId = id;
		}
		
		//node 0
		node[0].addLink(node[1], 0.25);		
		node[0].addLink(node[8], 0.25);
		node[0].addLink(node[9], 0.25);
		node[0].addLink(node[7], 0.25);
	
		//node 1
		node[1].addLink(node[8], 0.25);
		node[1].addLink(node[9], 0.25);
		node[1].addLink(node[2], 0.25);
		
		//node 2
		node[2].addLink(node[1], 0.30);
		node[2].addLink(node[3], 0.30);
		
		//node 3
		node[3].addLink(node[4], 0.25);
		node[3].addLink(node[7], 0.25);
		node[3].addLink(node[8], 0.25);
		
		//node 4
		node[4].addLink(node[5], 0.30);
		node[4].addLink(node[7], 0.30);
		
		//node 5
		node[5].addLink(node[6], 0.25);
		node[5].addLink(node[7], 0.25);
		node[5].addLink(node[9], 0.25);
		
		//node 6
		node[6].addLink(node[7], 0.30);
		node[6].addLink(node[9], 0.30);
		
		//node 7
		node[7].addLink(node[8], 0.25);
		node[7].addLink(node[9], 0.25);
		
		//node 8
		node[8].addLink(node[9], 0.25);
		
		//node 9 0,1,7,8 already set

	}
	
	//Setting  selling Area Setting, add selling area and radar link info
	public static void initialSellingAreaSetting(int numSellingArea,sellingArea[] sellAreasNode,RadarNode[] node)
	{
		for(int i =0;i<numSellingArea;i++)
		{
			sellAreasNode[i] = new sellingArea();
			String id = "SA"+i;  //selling area 
			sellAreasNode[i].areaId = id;
			
		}
		
		//selling area 0
		sellAreasNode[0].addLink(node[0], 0.2);
		sellAreasNode[0].addLink(node[1], 0.2);
		sellAreasNode[0].addLink(node[7], 0.2);
		sellAreasNode[0].addLink(node[8], 0.2);
		sellAreasNode[0].addLink(node[9], 0.2);
		
		//selling area 1
		sellAreasNode[1].addLink(node[1], 0.5);
		sellAreasNode[1].addLink(node[2], 0.5);
		
		//selling area 2
		sellAreasNode[2].addLink(node[3], 0.33);
		sellAreasNode[2].addLink(node[2], 0.33);
		sellAreasNode[2].addLink(sellAreasNode[3], 0.33);
		
		//selling area 3
		sellAreasNode[3].addLink(node[8], 0.5);
		//sellAreasNode[3].addLink(sellAreasNode[2], 0.5);
		
		//selling area 4
		sellAreasNode[4].addLink(node[3], 0.33);
		sellAreasNode[4].addLink(node[7], 0.33);
		
		//selling area 5
		sellAreasNode[5].addLink(node[3], 0.5);
		sellAreasNode[5].addLink(node[4], 0.5);
		
		//selling area 6
		sellAreasNode[6].addLink(node[4], 0.5);
		sellAreasNode[6].addLink(node[5], 0.5);
		
		//selling area 7
		sellAreasNode[7].addLink(node[5], 0.33);
		sellAreasNode[7].addLink(node[6], 0.33);
		sellAreasNode[7].addLink(node[7], 0.33);
		
		//selling area 8
		sellAreasNode[8].addLink(node[6], 0.33);
		sellAreasNode[8].addLink(node[9], 0.33);
		sellAreasNode[8].addLink(sellAreasNode[9], 0.33);
		
		//selling area 9
		//sellAreasNode[9].addLink(sellAreasNode[8], 0.33);
		
	}
	
	//add goods list
	//reading data from csv file"goodslist"
	//Goods name(not mandatory needed)||Goods ID||Purchasing Possibility||in selling area  
	public static purchaseList initialGoodsInitial(String goodsList,sellingArea[] sellAreasNode) throws IOException
	{
		purchaseList retPurList = new purchaseList();
		//ArrayList<goods> missingList = new ArrayList<product>();
		//String filepath = strMasterInventory;
		
		Reader in = new FileReader(goodsList);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
		for (CSVRecord record : records) {
			sellingArea curArea = sellAreasNode[Integer.parseInt(record.get(3))];
			goods curgood = new goods(Integer.parseInt(record.get(1)), Double.parseDouble(record.get(2)), curArea);
			//missingList.add(misProduct);
			retPurList.allGoodsList.add(curgood);
		}
		
		return retPurList;
	}
	
	//find all goods's areas in purchase lists and saves into a areaList(return);
	public static ArrayList<area> goodsListToAreaList(ArrayList<goods> goodsList){
		ArrayList<area> areaList = new ArrayList<area>();
		
		for(goods g: goodsList){
			areaList.add(g.getInArea());
		}

		System.out.println("\n areaList: ");
		for(area a: areaList){
			System.out.print(a.areaId+" ");
		}
		System.out.println();
		return areaList;
	}
	
	//find path, input a single customer purchasing goods in area records
	//find the smallest path for finding all goods
	//change pathList for Rifd record
	public static ArrayList<area> findPath(ArrayList<area> goodsAreaList){
		
		ArrayList<area> retList = new ArrayList<area>();
		
		Queue<area> queue = new LinkedList<area>();
		queue.add(node[0]);//at first add start point node[0] (RadarNode)	
		retList.add(node[0]);
		int nextGoodsAreaNo = 0;
		
		HashMap<area, area> thisComeFrom = new HashMap<area, area>(); 
		//key: this node, value: it comes from, 
		//this variable is used for back tracking the path in BFS
		
			while( !queue.isEmpty()&&nextGoodsAreaNo<goodsAreaList.size()){
			
				area curArea = queue.poll();
				for(area ar:curArea.link.keySet()){
	
					if(retList.contains( goodsAreaList.get(nextGoodsAreaNo) ) ){
						nextGoodsAreaNo++;
						break;
					}//been passed by already-->purchased already
					
					if(!thisComeFrom.containsKey(ar)){
						//System.out.println("thisComeFrom add key "+ ar.areaId+" comef value " +curArea.areaId);
						thisComeFrom.put(ar, curArea);
						queue.add(ar);
					}
					//add comeFrom record
					if(goodsAreaList.get(nextGoodsAreaNo).equals(ar)) {
	
						ArrayList<area> tempList = new ArrayList<area>();
						while(true){
							//System.out.println("last of retList  "+retList.get(retList.size()-1).areaId);
							tempList.add(ar);
							ar = thisComeFrom.get(ar); 
							//System.out.println(ar.areaId);
							if(ar.equals( retList.get(retList.size()-1) )) break;
						}
						tempList.add(ar);
						for(int i = tempList.size()-1; i >=0 ;i--){
							retList.add( tempList.get(i) ); 
						}//reverse tempList's value add to retList
						
						queue.clear();
						thisComeFrom.clear();
						queue.add(goodsAreaList.get(nextGoodsAreaNo));//next time start area should be this one
						nextGoodsAreaNo++;
						break;
						
					}//find it!! add this path to retList
					
			}//while
			
		}
			
			
		//at end add path to node[0] (RadarNode)		
		
		
		
		//Here I use Greedy + BFS, 
		//keep search for neighbor area(Radar/SellingArea) 
		//Till reach the next goods' area
	    //if curArea is in pathList, then skip.(has already passed-->purchased in here)
		
		
		return retList;
	}

	public static void printAreaList(ArrayList<area> list){
		for(area ar:list){
			System.out.print(ar.areaId+" ");
		}
		System.out.println();
	}

}

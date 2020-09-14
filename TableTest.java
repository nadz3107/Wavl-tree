import java.util.ArrayList;
import java.util.Collections;

public class TableTest {

	public static final int NUMBER = 20;
	
	public static void main(String[] args) {
		
		int i = 1;
		
//		for (int i = 1; i <= 10; i++)
//		{
			double countInsert = 0;
			double counDelete = 0;
			ArrayList<Integer> list = new ArrayList<Integer>();
			ArrayList<Integer> rebalnce = new ArrayList<>();
			for (int j = 0; j <= NUMBER * i; j++) {
				list.add(j);
			}

			Collections.shuffle(list);
			WAVLTree tree = new WAVLTree();
			
			for (int l = 0; l <= NUMBER * i; l++) {
				int num = list.get(l);
				int s = tree.insert(num, "value");
				rebalnce.add(s);
				countInsert += s;
			}
			
			System.out.println("tree after insert : ");
			System.out.println(tree);
			System.out.println("Total number of balancing actions after " + 10000 * i + " insertion:" + countInsert);
			double avg = countInsert/(NUMBER * i);
			System.out.println("avg number of balancing actions after " + 10000 * i + " insertion:" + Double.toString(avg));
			System.out.println("max = " + Collections.max(rebalnce));
			
			for (int l = 0; l <= NUMBER * i; l++) {
				System.out.println("DELETE number :" + l);
				counDelete += tree.delete(l);
				System.out.println(tree);
			}
			
			System.out.println("Total number of balancing actions after " + 10000 * i + " deletion:" + counDelete);
			double avgD = counDelete/(NUMBER * i);
			System.out.println("avg number of balancing actions after " + 10000 * i + " deletion:" +  Double.toString(avgD));
			System.out.println(tree.empty());
		}

//	}

}



import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
 
public class WAVLTester {
    private ArrayList<Test> _Tests;
    private int _maxOperPerTest;
   
     public static void main(String[] args) {
        int numOfTests = 1000;
        int maxOperationsInEachTest = 100;
        WAVLTester tester = new WAVLTester(maxOperationsInEachTest);
        for (int i = 0; i < numOfTests; ++i) {
            String result = tester.RunNewTest();
        //    if (!result.startsWith("Result: OK"))
                System.out.println(result);
        }
    }
 
    public WAVLTester(int maxOperationsPerTest) {
        _maxOperPerTest = maxOperationsPerTest;
        _Tests = new ArrayList<>();
    }
 
    public WAVLTester(String Commands) {
        String[] parts = Commands.split(";");
        Operation[] os = new Operation[parts.length];
        int index = 0;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].trim().equals(""))
                continue;
            String[] inner = parts[i].split("\\(", 2);
            String numStr = inner[1].replaceAll("\\)", "").trim();
            if (inner[0].trim().equals("Insert")) {
                os[index] = new Operation(OperType.Insert, Integer.parseInt(numStr), numStr);
                index++;
            } else if (inner[0].trim().equals("Delete")) {
                os[index] = new Operation(OperType.Delete, Integer.parseInt(numStr), numStr);
                index++;
            } else if (inner[0].trim().equals("NotExistingKeyDelete")) {
                os[index] = new Operation(OperType.NotExistingKeyDelete, Integer.parseInt(numStr), numStr);
                index++;
            } else if (inner[0].trim().equals("ExistingKeyInsert")) {
                os[index] = new Operation(OperType.ExistingKeyInsert, Integer.parseInt(numStr), numStr);
                index++;
            }
        }
 
        Test t = new Test(os);
        Error_Code c = t.ExecuteTest();
        System.out.println(t.toString());
    }
 
    public WAVLTester() {
        _maxOperPerTest = 40;
        _Tests = new ArrayList<>();
    }
 
    ArrayList<String> bads = new ArrayList<>();
 
    public String RunNewTest() {
        Test t = new Test(_maxOperPerTest);
        _Tests.add(t);
        Error_Code c = t.ExecuteTest();
        if (c != null && c != Error_Code.OK)
            bads.add(t.toString());
        return t.toString();
    }
 
    public class Test {
        private static final int MIN_OPER_COUNT = 5;
        private int _operationsCount;
        private Operation[] _operations;
 
        WAVLTree _tree;
        private ArrayList<Integer> _existingKeys;
        boolean preMade = false;
        Error_Code _status;
 
        public Test(int maxOperPerTest) {
            _status = null;
            if (maxOperPerTest < MIN_OPER_COUNT)
                maxOperPerTest = MIN_OPER_COUNT;
            _tree = new WAVLTree();
 
            Random r = new Random();
            _operationsCount = r.nextInt(maxOperPerTest - MIN_OPER_COUNT + 1) + MIN_OPER_COUNT;
            _operations = new Operation[_operationsCount];
            _existingKeys = new ArrayList<Integer>();
        }
 
        public Test(Operation[] os) {
            _status = null;
            _operations = os;
            _tree = new WAVLTree();
            _operationsCount = os.length;
            _existingKeys = new ArrayList<Integer>();
            preMade = true;
        }
 
        public Error_Code ExecuteTest() {
            if (preMade) {
                for (int i = 0; i < _operationsCount; i++) {
                    if (_operations[i] == null)
                        continue;
                    RunOperation(i); // run
                    _status = CheckTree();// check
                    if (_status != Error_Code.OK) {
                        PrepareStr(_status);
                        return _status;
                    }
                }
                PrepareStr(Error_Code.OK);
                return Error_Code.OK;
            }
            for (int i = 0; i < _operationsCount; i++) {
                try {
                    CreateOperation(i);    // create
                    RunOperation(i); // run
                    _status = CheckTree();// check
                    if (_status != Error_Code.OK) {
                        PrepareStr(_status);
                        return _status;
                    }
                } catch (Throwable e) {
                    System.out.println("id=" + _tree.hashCode());
                    System.out.println(_tree);
                    //System.out.println("Is stable? " + WAVLTester.isStable(_tree.getRoot()));
                    System.out.flush();
                    e.printStackTrace(System.out);
                    System.out.flush();
                    PrepareStr(Error_Code.Crash);
                    System.out.println(str);
                    System.exit(0);
                    return Error_Code.Crash;
                }
            }
            PrepareStr(Error_Code.OK);
            return Error_Code.OK;
        }
 
        private void CreateOperation(int operIndex) {
            Random r = new Random();
 
            // if empty - only allow bad delete or insert
            if (_existingKeys.isEmpty()) {
                OperType t = r.nextInt(100) < 75 ? OperType.Insert : OperType.NotExistingKeyDelete;
                int key = getNewKey();
                String info = "" + key;
                _operations[operIndex] = new Operation(t, key, info);
                return;
            }
 
            int type = r.nextInt(100);
            OperType t;
            if (type < 65)
                t = OperType.Insert;
            else if (type < 90)
                t = OperType.Delete;
            else if (type < 95)
                t = OperType.NotExistingKeyDelete;
            else
                t = OperType.ExistingKeyInsert;
 
            int key = 0;
            if (t == OperType.Insert || t == OperType.NotExistingKeyDelete)
                key = getNewKey();
            else if (t == OperType.Delete || t == OperType.ExistingKeyInsert)
                key = getRandomExistingKey();
 
            //if (t == OperType.Delete)
            //{
            //WAVLTree.WAVLNode deleteMe = _tree.findbyKey(key, _tree.getRoot());
            //if (!deleteMe.getLeft().isInnerNode() || !deleteMe.getRight().isInnerNode())
            //{
            //CreateOperation(operIndex);
            //return;
            //}
            //}
 
            _operations[operIndex] = new Operation(t, key, key + "");
        }
 
        private int getNewKey() {
            Random r = new Random();
            int i;
            while (true) {
                i = r.nextInt(_operationsCount * 100);
                if (!_existingKeys.contains(i)) {
                    return i;
                }
            }
        }
 
        private int getRandomExistingKey() {
            Random r = new Random();
            return _existingKeys.get(r.nextInt(this._existingKeys.size()));
        }
 
        private void RunOperation(int i) {
            if (i >= _operationsCount || _operations[i] == null)
                return;
 
            OperType t = _operations[i].getOperationType();
            int k = _operations[i].getKey();
            String info = _operations[i].getValue();
            if (t == OperType.Insert) {
                _tree.insert(k, info);
                _existingKeys.add(k);
            } else if (t == OperType.ExistingKeyInsert)
                _tree.insert(k, info);
            else if (t == OperType.Delete) {
                _tree.delete(k);
                _existingKeys.remove((Integer) k);
            } else
                _tree.delete(k);
        }
 
        private Error_Code CheckTree() {
            if (!checkBST())
                return Error_Code.Not_Binary_SearchTree;
            if (!checkSubSizes())
                return Error_Code.Bad_Sub_Tree_Size;
            if (!checkSize())
                return Error_Code.Bad_Size;
            if (!checkRanks())
                return Error_Code.Bad_WAVL_Ranks;
            if (!checkBalanced())
                return Error_Code.Not_Balanced;
            int res = checkNodes();
            if (res == 1)
                return Error_Code.Missing_Nodes;
            else if (res == 2)
                return Error_Code.Has_Deleted_Nodes;
            if (!checkExtNodes())
                return Error_Code.Bad_External_Nodes;
            if (!checkIfEmptyIsEmpty())
                return Error_Code.empty_Should_Be_True;
            if (!checkSearch()) {
                return Error_Code.Bad_Search;
            }
            if (!checkMin())
                return Error_Code.Bad_Min;
            if (!checkMax())
                return Error_Code.Bad_Max;
 
            if (!checkKeysToArray())
                return Error_Code.Bad_Keys_to_Array;
            if (!checkInfoToArray())
                return Error_Code.Bad_Info_to_Array;
 
            if (!checkSelect()) {
                checkSelect();
                return Error_Code.Bad_Select;
            }
 
            return Error_Code.OK;
        }
 
        private boolean checkBST() {
            return checkBST_rec(_tree.getRoot(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
 
        private boolean checkBST_rec(WAVLTree.WAVLNode node, int min, int max) {
            if (node == null || !node.isInnerNode())
                return true;
            return node.getKey() >= min && node.getKey() <= max &&
                    checkBST_rec(node.getLeft(), min, node.getKey() - 1) &&
                    checkBST_rec(node.getRight(), node.getKey() + 1, max);
        }
 
        private boolean checkSubSizes() {
            return checkSubSizes_Rec(_tree.getRoot());
        }
 
        private boolean checkSubSizes_Rec(WAVLTree.WAVLNode node) {
            if (node == null || !node.isInnerNode())
                return true;
 
            WAVLTree.WAVLNode left = node.getLeft();
            WAVLTree.WAVLNode right = node.getRight();
 
            int leftCount = (left != null && left.isInnerNode()) ? left.getSubtreeSize() : 0;
            int rightCount = (right != null && right.isInnerNode()) ? right.getSubtreeSize() : 0;
            if (node.getSubtreeSize() == (leftCount + rightCount + 1) &&
                    checkSubSizes_Rec(node.getLeft()) &&
                    checkSubSizes_Rec(node.getRight())) {
                return true;
            }
 
            return false;
        }
 
        private boolean checkBalanced() {
            //badHeightDiff = false;
            if (_tree.empty())
                return true;
 
            int k_rankOfRoot = ((WAVLTree.WAVLNode) _tree.getRoot()).getRank();
            int n_nodeCount = _tree.size();
            int h_height = height(_tree.getRoot());
 
            return (k_rankOfRoot >= h_height && k_rankOfRoot <= 2 * h_height) && (k_rankOfRoot <= 2 * (Math.log(n_nodeCount) / Math.log(2)));
            //return !badHeightDiff;
        }
 
        //boolean badHeightDiff = false;
        private int height(WAVLTree.WAVLNode n) {
            if (n == null || !n.isInnerNode())
                return -1;
 
            int leftHeight = height(n.getLeft());
            int rightHeight = height(n.getRight());
            //if (Math.abs(leftHeight - rightHeight) > 1)
            //badHeightDiff = true;
            return Integer.max(leftHeight, rightHeight) + 1;
        }
 
        private boolean checkRanks() {
            if (_tree.empty())
                return true;
 
            boolean res = checkRank_rec((WAVLTree.WAVLNode) _tree.getRoot());
            return res;
        }
 
        private boolean checkRank_rec(WAVLTree.WAVLNode node) {
            if (node == null || (!node.isInnerNode() && node.getRank() == -1))
                return true;
 
            int leftRank = node.getLeft() != null ? ((WAVLTree.WAVLNode) node.getLeft()).getRank() : -1;
            int rightRank = node.getRight() != null ? ((WAVLTree.WAVLNode) node.getRight()).getRank() : -1;
 
            boolean isType1_1 = (node.getRank() - leftRank) == 1 && (node.getRank() - rightRank) == 1;
            boolean isType1_2 = (node.getRank() - leftRank) == 1 && (node.getRank() - rightRank) == 2;
            boolean isType2_1 = (node.getRank() - leftRank) == 2 && (node.getRank() - rightRank) == 1;
            boolean isType2_2 = (node.getRank() - leftRank) == 2 && (node.getRank() - rightRank) == 2;
            if (!isType1_1 && !isType1_2 && !isType2_1 && !isType2_2)
                return false;
 
            return checkRank_rec((WAVLTree.WAVLNode) node.getLeft()) && checkRank_rec((WAVLTree.WAVLNode) node.getRight());
        }
 
        private int checkNodes() {
            ArrayList<Integer> shouldExistsKeys = new ArrayList<>(_existingKeys);
 
            boolean res = containsNodes_rec(_tree.getRoot(), shouldExistsKeys);
            if (!res)
                return 2;
            if (shouldExistsKeys.isEmpty())
                return 0;
            return 1;
        }
 
        private boolean containsNodes_rec(WAVLTree.WAVLNode node, ArrayList<Integer> shouldExistsKeys) {
            if (node == null || !node.isInnerNode())
                return true;
 
            if (shouldExistsKeys.contains((Integer) node.getKey())) {
                shouldExistsKeys.remove((Integer) node.getKey());
                return containsNodes_rec(node.getLeft(), shouldExistsKeys) && containsNodes_rec(node.getRight(), shouldExistsKeys);
            }
            return false;
        }
 
        private boolean checkExtNodes() {
            if (_tree.empty())
                return true;
 
            return checkExtNodes_rec(_tree.getRoot());
        }
 
        private boolean checkExtNodes_rec(WAVLTree.WAVLNode node) {
            if (node == null)
                return false;
 
            boolean leftNull = node.getLeft() == null;
            boolean rightNull = node.getRight() == null;
            if (leftNull && rightNull) // both null - this node must be external
                return !node.isInnerNode();
 
            if ((leftNull && !rightNull) || (rightNull && !leftNull)) // one null and one not null - not good
                return false;
 
            return node.isInnerNode() && checkExtNodes_rec(node.getLeft()) && checkExtNodes_rec(node.getRight());
        }
 
        private boolean checkIfEmptyIsEmpty() {
            if (_existingKeys.isEmpty())
                return _tree.empty();
            return true;
        }
 
        private boolean checkSearch() {
            // try to find non existing keys
            for (int ind = 0; ind < 5; ind++) {
                int newKey = getNewKey();
                String treeSearch = _tree.search(newKey);
                if (treeSearch != null)
                    return false;
            }
 
            //try to find all existing keys
            for (int i = 0; i < _existingKeys.size(); i++) {
                String fromSearch = _tree.search(_existingKeys.get(i));
                String fromTest = "" + _existingKeys.get(i);
                if (!fromSearch.equals(fromTest))
                    return false;
            }
 
            return true;
        }
 
        private boolean checkMin() {
            String minTree = _tree.min();
            if (_existingKeys.isEmpty())
                return minTree == null;
 
            int min = _existingKeys.get(0);
            for (int i = 1; i < _existingKeys.size(); i++) {
                if (_existingKeys.get(i) < min)
                    min = _existingKeys.get(i);
            }
            return minTree.equals("" + min);
        }
 
        private boolean checkMax() {
            String maxTree = _tree.max();
            if (_existingKeys.isEmpty())
                return maxTree == null;
 
            int max = _existingKeys.get(0);
            for (int i = 1; i < _existingKeys.size(); i++) {
                if (_existingKeys.get(i) > max)
                    max = _existingKeys.get(i);
            }
            return maxTree.equals("" + max);
        }
 
        private boolean checkKeysToArray() {
            int[] treeKeys = _tree.keysToArray();
 
            if (treeKeys.length != _existingKeys.size())
                return false;
 
            Collections.sort(_existingKeys);
 
            for (int i = 0; i < _existingKeys.size(); i++) {
                if (treeKeys[i] != _existingKeys.get(i))
                    return false;
            }
            return true;
        }
 
        private boolean checkInfoToArray() {
            String[] treeKeys = _tree.infoToArray();
 
            if (treeKeys.length != _existingKeys.size())
                return false;
 
            Collections.sort(_existingKeys);
 
            for (int i = 0; i < _existingKeys.size(); i++) {
                if (!treeKeys[i].equals("" + _existingKeys.get(i)))
                    return false;
            }
            return true;
        }
 
        private boolean checkSize() {
            return _tree.size() == _existingKeys.size();
        }
 
        private boolean checkSelect() {
            int size = _tree.size();
            if (_tree.empty() && _tree.select(0) != null)
                return false;
 
            if (size != _existingKeys.size())
                return false;
 
            Collections.sort(_existingKeys);
            for (int i = 1; i <= size; i++) {
                String treeSelect = _tree.select(i);
                if (treeSelect == null || !treeSelect.equals("" + _existingKeys.get(i - 1))) {
                    treeSelect = _tree.select(i);
                    return false;
                }
            }
 
            return true;
        }
 
        private void PrepareStr(Error_Code code) {
            str = "Result: " + code + ", Operations: ";
            boolean addedAtLeastOne = false;
            for (int i = 0; i < _operationsCount; i++) {
                if (_operations[i] != null) {
                    addedAtLeastOne = true;
                    str += _operations[i].getOperationType() + " (" + _operations[i].getKey() + ") ; ";
                }
            }
            if (!addedAtLeastOne)
                str = null;
 
 
        }
 
        private String str;
 
        public String toString() {
            if (str == null || str == "")
                return "Test Not Run";
            return str;
 
        }
    }
 
    public enum Error_Code {
        OK,
        Not_Binary_SearchTree,
        Bad_Sub_Tree_Size,
        Not_Balanced,
        Bad_WAVL_Ranks,
        Missing_Nodes,
        Has_Deleted_Nodes,
        Bad_External_Nodes,
        empty_Should_Be_True,
        Bad_Search,
        Bad_Min,
        Bad_Max,
        Bad_Keys_to_Array,
        Bad_Info_to_Array,
        Bad_Size,
        Bad_Select,
        Crash
    }
 
    public class Operation {
        private OperType _type;
        private int _key;
        private String _info;
 
        /**
         * Operation ctor
         *
         * @param type - Insert or Delete
         * @param key  - the node's key to insert / delete
         * @param info - the info to insert. Not relevant for deletion
         */
        public Operation(OperType type, int key, String info) {
            _type = type;
            _key = key;
            _info = info;
        }
 
 
        public int getKey() {
            return _key;
        }
 
        public String getValue() {
            return _info;
        }
 
        public OperType getOperationType() {
            return _type;
        }
    }
 
    public enum OperType {
        Insert,
        Delete,
        NotExistingKeyDelete,
        ExistingKeyInsert
    }
}
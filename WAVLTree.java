import java.util.function.BiConsumer;

/**
 * 
 * @author eyal borovsky(300075728)_rimon shy (302989215)
 * @version 1.0.0
 */

/**
 *
 * WAVLTree
 *
 * An implementation of a WAVL Tree. (Haupler, Sen and Tarajan ‘15)
 *
 */
public class WAVLTree {

	private enum Operation {
		FINISH, PROMOTE, ROTATION, DOUBLE_ROTATION, DEMOTE, NONE
	}

	private enum SIDE {
		LEFT, RIGHT, NONE
	}

	private WAVLNode root;
	private static Operation status;

	/**
	 * CTOR
	 */
	public WAVLTree() {
		root = null;
	}

	/**
	 * public boolean empty()
	 *
	 * @returns @true if and only if the tree is empty O(1)
	 */
	public boolean empty() {
		return size() == 0 ? true : false;
	}

	/**
	 * public String search(int k)
	 *
	 * @returns @String The info of an item with key k if it exists in the tree
	 *          otherwise return null O(log n)
	 */
	public String search(int key) {
		WAVLNode node = searchForNode(key);
		if (node != null) {
			if (node.isInnerNode())
				return node.value;
		}
		return null;
	}

	/**
	 * Search For Node by key
	 * 
	 * @param k
	 *            - is the key
	 * @return @WAVLNODE the node of the key, if the tree is not empty and if the
	 *         key isn't found return external node
	 */
	private WAVLNode searchForNode(int k) {
		WAVLNode node = root;
		while (node != null) {
			if (k == node.getKey())
				break;
			else if (node.getKey() == WAVLNode.EXTERNAL_NODE_RANK)
				return node;
			else if (k < node.getKey())
				node = node.getLeft();
			else
				node = node.getRight();
		}
		return node;
	}

	/**
	 *
	 * inserts an item with key k and info i to the WAVL tree. the tree must remain
	 * valid (keep its invariants). returns the number of rebalancing operations, or
	 * 0 if no rebalancing operations were necessary. returns -1 if an item with key
	 * k already exists in the tree.
	 * 
	 * @param k
	 * @param value
	 * @return rebalnce O (log n )
	 */
	public int insert(int k, String value) {
		int rebalancing = 0;
		WAVLNode searchNode = searchForNode(k);
		if (searchNode == null) {
			// insert first item to the tree, root is a leaf
			root = new WAVLNode(k, value);
			return rebalancing;
		} else if (searchNode.isInnerNode())
			// the key already exists
			return -1;

		// now we have external node (key = -1) in searchNode
		// create leaf
		WAVLNode newNode = new WAVLNode(k, value);
		WAVLNode parent = searchNode.parent;

		// connectNodeToParent
		/****
		 * rimon - you already check it in the code, eyal - where????????
		 ***/
		if (!parent.isLeaf()) {
			// insert to unary node
			status = Operation.FINISH;
		} else {
			// insert to leaf
			status = Operation.PROMOTE;
		}
		if (parent.key < k) {
			parent.right = newNode;
		} else {
			parent.left = newNode;
		}
		// connect to parent
		newNode.parent = parent;

		// start passing on the tree from the node until the tree is valid
		WAVLNode n = newNode;
		SIDE side = SIDE.NONE;
		while (status != Operation.FINISH) {
			// between n to n.parent
			if ((side = checkPromoteCase(n)) != SIDE.NONE) {
				status = Operation.PROMOTE;
				Operation rotateCase = checkRotationCase(n, side);
				if (rotateCase != Operation.NONE) {
					status = rotateCase;
				}
			} else {
				status = Operation.FINISH;
			}

			switch (status) {
			case PROMOTE:
				n.parent.rank++;
				++rebalancing;
				break;
			case ROTATION:
				singleRotation(n, side);
				status = Operation.FINISH;
				rebalancing += 2;
				break;
			case DOUBLE_ROTATION:
				// this is node x from the lecture
				// rimon please fix it to return the node to update the tree subTree and rank
				// until the root
				doubleRotation(n, side);
				rebalancing += 5;
				status = Operation.FINISH;
				break;
			default:
				break;
			}
			n = n.parent;
		}
		updateSubTreeSizeFromNodeToRoot(newNode.parent);
		return rebalancing;
	}

	/**
	 * update Sub Tree Size From Node To Root O(log n)
	 * 
	 * @param node
	 */
	private void updateSubTreeSizeFromNodeToRoot(WAVLNode node) {
		while (node != null) {
			if (node.key == WAVLNode.EXTERNAL_NODE_RANK) {
				node = node.parent;
				continue;
			} else {
				calculateSubTreePairNode(node);
			}
			node = node.parent;
		}
	}

	/**
	 * calculate Sub Tree Pair Node
	 * 
	 * @param node
	 *            O(1)
	 */
	private void calculateSubTreePairNode(WAVLNode node) {
		node.subTreeSize = 0;
		if (!node.isLeaf())
			if (node.right.isInnerNode())
				node.subTreeSize += node.right.subTreeSize + 1;
		if (node.left.isInnerNode())
			node.subTreeSize += node.left.subTreeSize + 1;
	}

	/**
	 * * checks if after insertion the balancing case is rotation, double rotation
	 * or none(only promote)
	 * 
	 * @param node
	 * @param side
	 *            - of node x to his parent z
	 * @return side - the operation status O (1)
	 */
	private Operation checkRotationCase(WAVLNode node, SIDE side) {
		Operation s = Operation.NONE;
		if (side == SIDE.LEFT) {
			if (getRankDiffBySide(node.parent, true) == 2) {
				if (getRankDiffBySide(node, true) == 2) {
					status = Operation.ROTATION;
				} else {
					status = Operation.DOUBLE_ROTATION;
				}
			}
		} else {
			// false==left
			if (getRankDiffBySide(node.parent, false) == 2) {
				if (getRankDiffBySide(node, false) == 2) {
					status = Operation.ROTATION;
				} else {
					status = Operation.DOUBLE_ROTATION;
				}
			}
		}
		return s;
	}

	/**
	 * between current node to the parent
	 * 
	 * @param node
	 * @return side of node x to his parent z O (1)
	 */
	private SIDE checkPromoteCase(WAVLNode node) {
		SIDE s = SIDE.NONE;

		if (node.parent == null) {
			return SIDE.NONE;
		}

		if (getRankDiffBySide(node.parent, false) == 0)
			s = SIDE.LEFT;
		else if (getRankDiffBySide(node.parent, true) == 0)
			s = SIDE.RIGHT;
		return s;
	}

	/**
	 * performs the double rotation
	 * 
	 * @param node
	 * @param side
	 * @return O(1)
	 */
	private void doubleRotation(WAVLNode node, SIDE side) {
		int bRank;
		WAVLNode z = node.parent;
		int zRank= z.rank;
		SIDE zSide = this.SideToParent(z.parent, z);
		WAVLNode b, c, d = null;
		if (side == SIDE.LEFT) {
			b = node.right;
			bRank= b.rank;
			c = b.left;
			d = b.right;
			node.right = c;
			c.parent = node;
			b.parent = z.parent;
			if (zSide == SIDE.LEFT)
				z.parent.left = b;
			else if (zSide == SIDE.RIGHT)
				z.parent.right = b;
			else
				root = b;
			node.parent = b;
			b.left = node;
			z.parent = b;
			b.right = z;
			d.parent = z;
			z.left = d;
		} else {
			b = node.left;
			bRank= b.rank;
			c = b.right;
			d = b.left;
			node.left = c;
			c.parent = node;
			b.parent = z.parent;
			if (zSide == SIDE.LEFT)
				z.parent.left = b;
			else if (zSide == SIDE.RIGHT)
				z.parent.right = b;
			else
				root = b;
			node.parent = b;
			b.right = node;
			z.parent = b;
			b.left = z;
			d.parent = z;
			z.right = d;
		}
		node.rank= bRank;
		z.rank= bRank;
		b.rank= zRank;
		calculateSubTreePairNode(node);
		calculateSubTreePairNode(z);
		calculateSubTreePairNode(b);

	}

	/**
	 * performs the rotation.
	 * 
	 * @param node
	 * @param side
	 *            - of node x to his parent z
	 * @return O(1)
	 */
	private void singleRotation(WAVLNode node, SIDE side) {
		WAVLNode z = node.parent;
		WAVLNode b = null;
		SIDE zSide = this.SideToParent(z.parent, z);

		if (side == SIDE.LEFT) {
			b = node.right;
			node.parent = z.parent;
			if (zSide == SIDE.LEFT)
				z.parent.left = node;
			else if (zSide == SIDE.RIGHT)
				z.parent.right = node;
			else // zSide is null
				root = node;
			z.parent = node;
			node.right = z;
			z.left = b;
			b.parent = z;

		} else {
			b = node.left;
			node.parent = z.parent;
			if (zSide == SIDE.LEFT)
				z.parent.left = node;
			else if (zSide == SIDE.RIGHT)
				z.parent.right = node;
			else // zSide is null
				root = node;
			z.parent = node;
			node.left = z;
			z.right = b;
			b.parent = z;
		}
		--z.rank;
		calculateSubTreePairNode(z);
	}

	/**
	 * checks the side of node x to his parent z.
	 * 
	 * @param parent
	 * @param chiled
	 * @return side of child O(1)
	 */
	private SIDE SideToParent(WAVLNode parent, WAVLNode chiled) {
		if (parent == null)
			return SIDE.NONE;
		if (parent.getLeft() == chiled) {
			return SIDE.LEFT;
		} else {
			return SIDE.RIGHT;
		}
	}

	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there; the tree
	 * must remain valid (keep its invariants). returns the number of rebalancing
	 * operations, or 0 if no rebalancing operations were needed. returns -1 if an
	 * item with key k was not found in the tree. O(log n)
	 * 
	 * @param k
	 *            - key
	 * 
	 * @return rebalncing
	 * 
	 */
	public int delete(int k) {
		int rebalancing = 0;
		// if the node not exist return -1
		WAVLNode searchNode = searchForNode(k);
		if (searchNode == null) {
			return -1;
		}
		if (searchNode.isInnerNode()) {
			rebalancing = this.delete(searchNode);
		}
		return rebalancing;
	}

	/**
	 * Delete node from tree of exists
	 * 
	 * @param searchNode
	 * @return rebalance count
	 */
	private int delete(WAVLNode searchNode) {
		int rebalancing = 0;
		WAVLNode rebalanceNode = null;
		WAVLNode parent = null;
		SIDE sideOfChild;
		SIDE sideToParent;

		// get parent side
		parent = searchNode.parent;
		sideToParent = SideToParent(parent, searchNode);

		// if is leaf
		if (searchNode.isLeaf()) {
			//System.out.println("searchNode is leaf - delete");
			WAVLNode externalNode = WAVLNode.createExternalNode(parent);
			switch (sideToParent) {
			case LEFT:
				parent.left = externalNode;
				break;
			case RIGHT:
				parent.right = externalNode;
				break;
			case NONE:
				// rimon, all the tree delete???
				root = null;
				return rebalancing;
			default:
				break;
			}
			externalNode.parent = parent;
			parent.subTreeSize--;
			if (parent.isLeaf()) {
				parent.rank = 0;
				rebalanceNode = parent.parent;
				rebalancing++;
				parent.subTreeSize = 0;
			} else
				rebalanceNode = parent;
		}
		// unary Node
		else if ((sideOfChild = searchNode.isUnary()) != SIDE.NONE) {
			//System.out.println("search node is Unary");
			if (sideOfChild == SIDE.LEFT) {
				if (sideToParent == SIDE.LEFT) {
					parent.left = searchNode.left;
				} else if (sideToParent == SIDE.NONE) {
					root = searchNode.left;
				} else {
					parent.right = searchNode.left;
				}
				searchNode.left.parent = parent;
			} else {
				if (sideToParent == SIDE.LEFT) {
					parent.left = searchNode.right;
				} else if (sideToParent == SIDE.NONE) {
					root = searchNode.right;
					root.rank = 0;
					root.subTreeSize = 0;
					root.parent = null;
				} else {
					parent.right = searchNode.right;
				}
				searchNode.right.parent = parent;
			}
			if (parent != null) {
				parent.subTreeSize--;
			}
			rebalanceNode = parent;
		}
		// 2 children
		else {
			//System.out.println("search node is 2 children");
			WAVLNode successor = getSuccessor(searchNode);
			if (successor == null) {
				root = null;
				return 0;
			} else {
				//System.out.println("the successor is : " + successor);
				rebalancing += this.delete(successor);
				// System.out.println(this.toString());
				parent = searchNode.parent;
				sideToParent = SideToParent(parent, searchNode);
				if (sideToParent == SIDE.LEFT) {
					parent.left = successor;
				} else if (sideToParent == SIDE.NONE) {
					root = successor;
				} else {
					parent.right = successor;
				}
				successor.left = searchNode.left;
				successor.right = searchNode.right;
				successor.subTreeSize = searchNode.subTreeSize;
				successor.rank = searchNode.rank;
				successor.parent = parent;
				successor.left.parent = successor;
				successor.right.parent = successor;
				rebalanceNode = successor;
			}
		}
		//System.out.println("befire rebalnce:");
		//System.out.println(this);
		//System.out.println(rebalanceNode);
		rebalancing += rebalance(rebalanceNode);
		updateSubTreeSizeFromNodeToRoot(rebalanceNode);
		return rebalancing;
	}

	/**
	 *
	 * rebalance the tree after the deletion of the node.
	 * 
	 * @param rebalanceNode
	 * 
	 * @return number of rebalacing
	 * 
	 *         O(log n)
	 */
	private int rebalance(WAVLNode rebalanceNode) {
		if (rebalanceNode == null) {
			return 0;
		}
		//System.out.println("check rebalanceNode = " + rebalanceNode);
		int rebalancing = 0;
		SIDE side = SIDE.NONE;
		status = Operation.NONE;
		Operation rotateCase = Operation.NONE;
		// checks which case of rebalancing is this
		while (status != Operation.FINISH) {
			//System.out.println(this);
			if ((side = checkDemoteCase(rebalanceNode)) != SIDE.NONE) {
				status = Operation.DEMOTE;
				if (side == SIDE.RIGHT)
					rotateCase = checkRotationCaseDeleate(rebalanceNode.left, side);
				else if (side == SIDE.LEFT)
					rotateCase = checkRotationCaseDeleate(rebalanceNode.right, side);
				if (rotateCase != Operation.NONE) {
					status = rotateCase;
				}
			} else {
				status = Operation.FINISH;
			}

			switch (status) {
			case DEMOTE:
				rebalanceNode.rank--;
				++rebalancing;
				break;
			case ROTATION:
				if (side == SIDE.LEFT)
					singleRotation(rebalanceNode.right, SIDE.RIGHT);
				else if (side == SIDE.RIGHT)
					singleRotation(rebalanceNode.left, SIDE.LEFT);
				status = Operation.FINISH;
				rebalancing += 2;
				break;
			case DOUBLE_ROTATION:
				if (side == SIDE.LEFT)
					doubleRotation(rebalanceNode.right, SIDE.RIGHT);
				else if (side == SIDE.RIGHT)
					doubleRotation(rebalanceNode.left, SIDE.LEFT);
				rebalancing += 6;
				status = Operation.FINISH;
				break;
			default:
				break;
			}
			if (rebalanceNode.parent == null)
				status = Operation.FINISH;
			rebalanceNode = rebalanceNode.parent;
		}
		if (rebalanceNode != null) {
			updateSubTreeSizeFromNodeToRoot(rebalanceNode.parent);
		}
		return rebalancing;

	}

	// node is Z in presentation
	/**
	 *
	 * checks if it's demote case, and if so, if it's the right or the left son
	 * 
	 * @param @WAVLNode
	 *            node
	 * 
	 * @return side
	 */
	private SIDE checkDemoteCase(WAVLNode node) {
		SIDE s = SIDE.NONE;
		// System.out.println("*********************************************************");
		// System.out.println(node);
		if (getRankDiffBySide(node, false) == 3)
			s = SIDE.LEFT;
		else if (getRankDiffBySide(node, true) == 3)
			s = SIDE.RIGHT;
		return s;
	}

	/**
	 * private Operation checkRotationCaseDeleate(WAVLNode node, SIDE side) {
	 *
	 * checks if after deletion the balancing case is rotation, double rotation or
	 * none(only demote)
	 * 
	 * @param node
	 * @param side
	 * 
	 * @return next @operation to do
	 */
	private Operation checkRotationCaseDeleate(WAVLNode node, SIDE side) {
		Operation status = Operation.NONE;
		if (side == SIDE.LEFT) {
			if (getRankDiffBySide(node.parent, true) == 1) {
				if (getRankDiffBySide(node.parent.right, true) == 1) {
					status = Operation.ROTATION;
				} else if (getRankDiffBySide(node.parent.right, true) == 2) {
					status = Operation.DOUBLE_ROTATION;
				}
			}

		} else {
			// false==left
			if (getRankDiffBySide(node.parent, false) == 1) {
				if (getRankDiffBySide(node.parent.left, false) == 1) {
					status = Operation.ROTATION;
				} else if (getRankDiffBySide(node.parent.left, false) == 2) {
					status = Operation.DOUBLE_ROTATION;
				}
			}
		}
		return status;
	}

	/**
	 * public String min()
	 *
	 * Returns the info of the item with the smallest key in the tree, or null if
	 * the tree is empty O(log n)
	 * 
	 * @return
	 */

	public String min() {
		WAVLNode node = getMinNode(root);
		if (node == null) {
			return null;
		}
		return node.getValue();
	}

	/**
	 * @return min key O (log n)
	 */
	public int minKey() {
		WAVLNode node = getMinNode(root);
		if (node == null) {
			return -1;
		}
		return node.key;
	}

	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree, or null if the
	 * tree is empty O(log n)
	 * 
	 * @return
	 */
	public String max() {
		WAVLNode node = root;
		while (node != null) {
			if (!node.getRight().isInnerNode()) {
				return node.getValue();
			}
			node = node.getRight();
		}
		return null;
	}

	/**
	 *
	 * Returns a sorted array which contains all keys in the tree, or an empty array
	 * if the tree is empty. O(n)
	 * 
	 * @return array contain the keys
	 */
	public int[] keysToArray() {
		final int[] arr = new int[size() + 1];
		BiConsumer<WAVLNode, Integer> biConsumer = (node, index) -> arr[index] = node.getKey();
		InOrderTree(root, 0, biConsumer);
		return arr;
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree, sorted by their
	 * respective keys, or an empty array if the tree is empty. O(n)
	 * 
	 * @return array contain the values
	 */
	public String[] infoToArray() {
		final String[] arr = new String[size() + 1];
		BiConsumer<WAVLNode, Integer> biConsumer = (node, index) -> arr[index] = node.getValue();
		InOrderTree(root, 0, biConsumer);
		return arr;
	}

	/**
	 * Arrange the tree in order to collection
	 * 
	 * @param node
	 * @param i
	 * @param biConsume
	 *            O (n)
	 */
	private void InOrderTree(WAVLNode node, int i, BiConsumer<WAVLNode, Integer> biConsumer) {
		if (node == null) {
			return;
		}
		WAVLNode n = getMinNode(root);
		while (n != null) {
			biConsumer.accept(n, i);
			++i;
			n = getSuccessor(n);
		}
	}

	/**
	 * public int size()
	 *
	 * Returns the number of nodes in the tree. O(1)
	 * 
	 * @return size
	 */
	public int size() {
		if (root != null) {
			return root.getSubTreeSize();
		}
		return 0; // to be replaced by student code
	}

	/**
	 * public WAVLNode getRoot()
	 *
	 * Returns the root WAVL node, or null if the tree is empty O(1)
	 * 
	 * @return root
	 */
	public WAVLNode getRoot() {
		return root;
	}

	/**
	 * public int select(int i)
	 *
	 * Returns the value of the i'th smallest key (return -1 if tree is empty)
	 * Example 1: select(1) returns the value of the node with minimal key Example
	 * 2: select(size()) returns the value of the node with maximal key Example 3:
	 * select(2) returns the value 2nd smallest minimal node, i.e the value of the
	 * node minimal node's successor
	 * 
	 * @param i
	 *            O(n) - we can do it better by const
	 * @return value
	 */
	public String select(int i) {
		WAVLNode node = getMinNode(root);
		if (node == null || i > size()) {
			return null;
		}
		for (int j = 1; j < i; j++) {
			node = getSuccessor(node);
		}
		return node.getValue();
	}

	/**
	 * 
	 * @param node
	 * @return The successor to the given node, or null if it has no successor. O
	 *         (log n)
	 */
	private WAVLNode getSuccessor(WAVLNode node) {
		if (node != null) {
			if (node.getRight().isInnerNode()) {
				return getMinNode(node.getRight());
			}
			WAVLNode y = node.getParent();
			while (y != null && node == y.getRight()) {
				node = y;
				y = node.getParent();
			}
			return y;
		}
		return null;
	}

	/**
	 * 
	 * @param rootNode
	 * @return the min node
	 */
	private WAVLNode getMinNode(WAVLNode rootNode) {
		WAVLNode node = rootNode;
		while (node != null) {
			if (!node.getLeft().isInnerNode()) {
				return node;
			}
			node = node.getLeft();
		}
		return null;
	}

	@Override
	public String toString() {
		return WAVLTreePrinter.toString(this);
	}

	/**
	 * @return The rank difference between the parent and the child on it's side. if
	 *         side is true rightRankDiff else leftRankDiff
	 */
	private static int getRankDiffBySide(WAVLNode parent, boolean side) {
		// System.out.println("getRankDiffBySide :" + parent);
		int r = side ? WAVLNode.getRankDiff(parent, parent.right) : WAVLNode.getRankDiff(parent, parent.left);
		return r;
	}

	/**
	 * public class WAVLNode
	 */

	public static class WAVLNode {
		static final int EXTERNAL_NODE_RANK = -1;
		private int key;
		private String value;
		private int rank;
		private WAVLNode parent;
		private WAVLNode left;
		private WAVLNode right;
		private int subTreeSize;

		/**
		 * create leaf
		 * 
		 * @param key
		 * @param value
		 */
		public WAVLNode(int key, String value) {
			this.key = key;
			this.value = value;
			rank = 0;
			subTreeSize = 0;
			this.left = WAVLNode.createExternalNode(this);
			this.right = WAVLNode.createExternalNode(this);
		}

		/**
		 * 
		 * @return is unary node
		 */
		public SIDE isUnary() {
			if (this.left.key != -1 && this.right.key == -1) {
				return SIDE.LEFT;
			} else if (this.left.key == -1 && this.right.key != -1)
				return SIDE.RIGHT;

			return SIDE.NONE;
		}

		/**
		 * private CTOR
		 */
		private WAVLNode() {
			key = -1;
			rank = EXTERNAL_NODE_RANK;
			subTreeSize = 0;
		}

		/**
		 * 
		 * @return if node is leaf
		 */
		public boolean isLeaf() {
			if (this.left.key == -1 && this.right.key == -1)
				return true;
			return false;
		}

		/**
		 * 
		 * create external node with key = -1
		 * 
		 * @param parent
		 * @return the node
		 */
		private static WAVLNode createExternalNode(WAVLNode parent) {
			WAVLNode node = new WAVLNode();
			node.parent = parent;
			node.rank = -1;
			node.subTreeSize = 0;
			return node;
		}

		/**
		 * 
		 * @return key, if the node is external node return -1
		 */
		public int getKey() {
			return key;
		}

		/**
		 * 
		 * @return the info of the nod. if the node is external node return null
		 */
		public String getValue() {
			return value;
		}

		/**
		 * 
		 * @return child left
		 */
		public WAVLNode getLeft() {
			return left;
		}

		/**
		 * 
		 * @return child right
		 */
		public WAVLNode getRight() {
			return right;
		}

		/**
		 * 
		 * @return rank
		 */
		public int getRank() {
			return rank;
		}

		/**
		 * 
		 * @param rank
		 */
		public void setRank(int rank) {
			this.rank = rank;
		}

		/**
		 * 
		 * @return parent
		 */
		public WAVLNode getParent() {
			return parent;
		}

		/**
		 * 
		 * @param parent
		 */
		public void setParent(WAVLNode parent) {
			this.parent = parent;
		}

		/**
		 * 
		 * @return Sub Tree Size
		 */
		public int getSubTreeSize() {
			return subTreeSize;
		}

		/**
		 * 
		 * @param subTreeSize
		 */
		public void setSubTreeSize(int subTreeSize) {
			this.subTreeSize = subTreeSize;
		}

		/**
		 * 
		 * @param value
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * 
		 * @param left
		 */
		public void setLeft(WAVLNode left) {
			this.left = left;
		}

		/**
		 * 
		 * @param right
		 */
		public void setRight(WAVLNode right) {
			this.right = right;
		}

		/**
		 * 
		 * @return true if the node is internal , otherwise false O(1)
		 */
		public boolean isInnerNode() {
			return (key != EXTERNAL_NODE_RANK ? true : false);
		}

		/**
		 * 
		 * @return the number of internal mode in the sub-tree O(1)
		 */
		public int getSubtreeSize() {
			return subTreeSize;
		}

		@Override
		public String toString() {
			return "Node(" + key + ", rank=" + rank + ")";
		}

		/**
		 * @param parent
		 * @param child
		 * @return The rank diff between the node and its child
		 */
		public static int getRankDiff(WAVLNode parent, WAVLNode child) {
			if (parent == null || child == null)
				throw new IllegalStateException("Left node cannot be null");
			return parent.getRank() - child.getRank();
		}
	}
}

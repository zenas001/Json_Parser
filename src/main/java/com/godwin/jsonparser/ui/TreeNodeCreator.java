package com.godwin.jsonparser.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * Created by Godwin on 4/21/2018 12:32 PM for json.
 *
 * @author : Godwin Joseph Kurinjikattu
 */
public class TreeNodeCreator {
    public static DefaultTreeModel getTreeModel(String jsonString) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

        String[] lines = jsonString.split("\n");
        List<String> list = Arrays.asList(lines);
        ListIterator<String> iterator = list.listIterator();

        createNode(iterator, root, 0);
        return new DefaultTreeModel(root);
    }

    public static DefaultTreeModel getTreeModelFromMap(Map<String, Object> jsonMap) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

        createNodeFromMap(jsonMap, root);
        return new DefaultTreeModel(root);
    }

    private static DefaultMutableTreeNode createNode(ListIterator<String> iterator, DefaultMutableTreeNode rootNode, int count) {

        if (!iterator.hasNext()) {
            return rootNode;
        }

        while (iterator.hasNext()) {
            String line = iterator.next();
            line = line.trim();
            if (line.endsWith("{") || line.endsWith("[")) {
                String sub;
                if (line.length() > 3) {
                    sub = line.substring(0, line.length() - 3);
                } else {
                    sub = "" + count;
                }
                if (line.equals("{")) count++;
                else count = 0;
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(sub);
                DefaultMutableTreeNode children = createNode(iterator, root, count);
                root.setUserObject(sub + "  {" + children.getChildCount() + "}");
                rootNode.add(children);
            } else {
                boolean isBreakable = true;
                while (true) {
                    if (line.endsWith("},") || line.endsWith("],") || line.endsWith("}") || line.endsWith("]")) {
                        if (line.length() > 2) {
                            String sub;
                            if (line.endsWith("}") || line.endsWith("]")) {
                                sub = line;
                            } else if (line.endsWith("},") || line.endsWith("],")) {
                                sub = line.substring(0, line.length() - 1);
                            } else {
                                sub = line.substring(0, line.length() - 2);
                                isBreakable = false;
                            }
                            DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(sub);
                            rootNode.add(dataNode);
                        }
                        break;
                    } else if (line.endsWith("{") || line.endsWith("[")) {
                        iterator.previous();
                        isBreakable = false;
                        break;
                    } else {
                        DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(line);
                        rootNode.add(dataNode);
                        if (iterator.hasNext()) {
                            line = iterator.next();
                            line = line.trim();
                        } else {
                            break;
                        }
                    }
                }
                if (isBreakable)
                    break;
            }
        }
        return rootNode;
    }

    private static DefaultMutableTreeNode createNode2(ListIterator<String> iterator, DefaultMutableTreeNode rootNode, int count) {

        if (!iterator.hasNext()) {
            return rootNode;
        }

        while (iterator.hasNext()) {
            String line = iterator.next();
            line = line.trim();
            if (line.endsWith("{") || line.endsWith("[")) {
                String sub;
                if (line.length() > 3) {
                    sub = line.substring(0, line.length() - 3);
                } else {
                    sub = "" + count;
                }
                if (line.equals("{")) count++;
                else count = 0;
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(sub);
                DefaultMutableTreeNode children = createNode2(iterator, root, count);
                root.setUserObject(sub + "  {" + children.getChildCount() + "}");
                rootNode.add(children);
            } else {
                if (line.endsWith(",")) {
                    String sub = "";
                    if (line.endsWith("},") || line.endsWith("],")) {
                        sub = line.substring(0, line.length() - 1);
                    } else {
                        sub = line;
                    }
                    DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(sub);
                    rootNode.add(dataNode);
                } else if (line.endsWith("{") || line.endsWith("[")) {
                    iterator.previous();
                    break;
                } else {
                    DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(line);
                    rootNode.add(dataNode);
                }
            }
        }
        return rootNode;
    }

    private static DefaultMutableTreeNode createNodeFromMap(Map<String, Object> jsonMap, DefaultMutableTreeNode rootNode) {
        if (null == jsonMap ||jsonMap.isEmpty()) {
            rootNode.setUserObject("{0}");
            return rootNode;
        }

        Iterator<Map.Entry<String, Object>> entries = jsonMap.entrySet().iterator();
        rootNode.setUserObject("{" + jsonMap.size() + "}");
        while (entries.hasNext()) {
            Map.Entry<String, Object> item = entries.next();
            if (item.getValue() instanceof Map) {
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(item.getKey());
                Map<String, Object> subMap = (Map<String, Object>) item.getValue();
                createNodeFromMap(subMap, subNode);
                rootNode.add(subNode);
            } else if (item.getValue() instanceof List) {
                List<Object> list = (List<Object>) item.getValue();
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(item.getKey() + " [" + list.size() + "]");
                for (Object obj : list) {
                    DefaultMutableTreeNode child;
                    if (null == obj) {
                        child = new DefaultMutableTreeNode("null");
                    } else {
                        child = new DefaultMutableTreeNode(obj.toString());
                        if (obj instanceof Map) {
                            Map<String, Object> subMap = (Map<String, Object>) obj;
                            createNodeFromMap(subMap, child);
                        }
                    }
                    subNode.add(child);
                }
                rootNode.add(subNode);
            } else {
                DefaultMutableTreeNode subNode;
                if (null == item.getValue()){
                    subNode = new DefaultMutableTreeNode(item.getKey() + ": null");
                }else {
                    subNode = new DefaultMutableTreeNode(item.getKey() + ": " + item.getValue().toString());
                }
                rootNode.add(subNode);
            }
        }
        return rootNode;
    }
}

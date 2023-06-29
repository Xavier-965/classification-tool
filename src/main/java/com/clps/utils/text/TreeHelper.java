package com.clps.utils.text;

import java.util.*;

/**
 * 将冒号分割的文本转成树型结构数据
 * @author tony
 * @date 2020/07/09
 * 用法<br/>
 * <pre>
 *         public static void main(String[] args) {
 *         String prefix = "com:clps:resume:";
 *         String[] dat = {"data1:a:b", "data1:a:b1", "data2:aa:c1"};
 *
 *         Node tree = genTreeByTextLines(Arrays.asList(dat), prefix);
 *         System.out.println("Done");
 *     }
 *     </pre>
 * </pre>
 */
public class TreeHelper {
    /**
     * 生成包含树根的完整的一棵树
     * @param lines 文本数据行
     * @param prefix 树根名称
     * @param spliter 文本数据分隔符
     * @return root
     */
    public static Node genTreeByTextLines(List<String> lines, String prefix,String spliter) {
        List<Node> all = new ArrayList<>();
        Map<String, Node> src = getAllNodes(lines, prefix,spliter);
        src.forEach((k, v) -> {
            all.add(v);
        });
        Node root = new Node(null, prefix, prefix);
        List<Node> tree = createTree(prefix, all);
        root.setChildren(tree);
        return root;
    }

    /**
     * 生成不带根节点的
     * @param lines 文本行
     * @param prefix 树根名称
     * @param spliter 文本数据分隔符号
     * @return 树
     */
    public static List<Node> genTreeWithoutRootByTextLines(List<String> lines, String prefix,String spliter) {
        List<Node> all = new ArrayList<>();
        Map<String, Node> src = getAllNodes(lines, prefix, spliter);
        src.forEach((k, v) -> {
            all.add(v);
        });
        Node root = new Node(null, prefix, prefix);
        List<Node> tree = createTree(prefix, all);
        root.setChildren(tree);
        return tree;
    }

    private static Map<String, Node> getAllNodes(List<String> subKeys, String prefix,String spliter) {
        Map<String, Node> all = new HashMap<>();
        for (String s : subKeys) {
            int index = s.lastIndexOf(spliter);
            if (index == -1) { // 叶节点
                all.put(s, new Node(prefix, s, s));

            } else {
                String tmp = s;
                while (true) {
                    if (index == -1) {
                        if (!all.keySet().contains(tmp)) {
                            all.put(tmp, new Node(prefix, tmp, tmp));
                        }
                        break;
                    }
                    String pid = tmp.substring(0, index);
                    String id = tmp;
                    String name = s.substring(index + 1, tmp.length());
                    if (!all.keySet().contains(id)) {
                        all.put(id, new Node(pid, id, name));
                    }
                    tmp = tmp.substring(0, index);
                    index = tmp.lastIndexOf(spliter);

                }
            }
        }
        return all;
    }

    private static List<Node> createTree(String pid, List<Node> menus) {
        List<Node> treeMenu = new ArrayList<>();
        for (Node menu : menus) {
            if (pid.equals(menu.getPid())) {
                treeMenu.add(menu);
                menu.setChildren(createTree(menu.getId(), menus));
            }
        }
        return treeMenu;
    }
}

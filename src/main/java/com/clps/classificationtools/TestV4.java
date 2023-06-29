package com.clps.classificationtools;

import com.clps.utils.TextParser;
import com.clps.utils.WordDictionary;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xavier
 * @date 20-5-6 下午2:55
 */
public class TestV4 {
    private JPanel JP;
    private JPanel JP1;
    private JPanel JP2;
    private JScrollPane JP2_1;
    private JScrollPane JP2_2;
    private JPanel JP3;
    private JButton filechoosebt;//文件选择
    private JButton entchoosebt;//确认按钮
    private JButton otherclassbt;//其他分类按钮

    private JButton beforebt;//上一条按钮
    private JButton nextbt;//下一条按钮
    private JTextField textField1;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JLabel label;
    private JFileChooser jfc = new JFileChooser();// 文件选择器
    private String FilePath = "";// 文件路径
    private File file = null;//目标文件
    private File filedir = null;//结果文件目录
    private File cachefile = null;//缓存文件
    private List<String> contentlist = new ArrayList<String>();
    private int linenumber = -1;
    private HashMap<String, String> hashMap = new HashMap<String, String>();
    private static JFrame frame = new JFrame("文件分类工具V4");
    private static List<String> classificationlist = new ArrayList<>();
    private int index = 0;
    private int btx = 0;
    private int bty = 0;
    private static Toolkit tk = Toolkit.getDefaultToolkit();

    public TestV4() {
        frame.addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                System.out.println("存储文件所在行!");
                BufferedWriter cacheout = null;
                if (file != null) {
                    if (cachefile.exists() && cachefile.isFile()) {
                        cachefile.delete();
                        try {
                            cachefile.createNewFile();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        try {
                            cachefile.createNewFile();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (!filedir.exists()) {
                        try {
                            filedir.createNewFile();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if (hashMap.size() != 0) {
                    try {
                        cacheout = new BufferedWriter(new FileWriter(cachefile, true));
                        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                            cacheout.write(entry.getKey() + "==" + entry.getValue() + "\n");
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                try {
                    if (cacheout != null) {
                        cacheout.close();
                    }
                } catch (IOException oe) {
                    oe.printStackTrace();
                }

            }

            public void windowClosed(WindowEvent e) {

            }

            public void windowIconified(WindowEvent e) {

            }

            public void windowDeiconified(WindowEvent e) {

            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });

        filechoosebt.addActionListener(
                (ActionEvent e) -> {
                    // 绑定到选择文件事件
                    if (e.getSource().equals(filechoosebt)) {// 判断触发方法的按钮是哪个
                        jfc.setFileSelectionMode(0);// 设定只能选择到文件
                        int state = jfc.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句
                        if (state == 1) {
                            return;// 撤销则返回
                        } else {
                            File f = jfc.getSelectedFile();// f为选择到的文件
                            FilePath = f.getAbsolutePath();
                            textField1.setText(FilePath);
                        }
                    }

                }
        );

        entchoosebt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                contentlist.clear();
                InputStream inputStream = null;
                // 绑定到选择文件，先择文件事件
                if (e.getSource().equals(entchoosebt)) {// 判断触发方法的按钮是哪个
                    String filepath = textField1.getText().trim();

                    file = new File(filepath);
//                    if (file.exists() && !file.isDirectory() && !filepath.equals(FilePath)) {
                    if (file.exists() && !file.isDirectory() ) {
                        filedir = new File(file.getParent() + "/target");
                        if (!filedir.exists()) {
                            filedir.mkdir();
                        }
                        cachefile = new File(filedir.getPath() + "/cache~");
                        FilePath = filepath;
                        if (cachefile.exists() && cachefile.isFile()) {
                            FileInputStream fileInputStream = null;
                            try {
                                fileInputStream = new FileInputStream(cachefile);
                                BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("UTF-8")));
                                while (br.ready()) {
                                    String line = br.readLine();
                                    try {
                                        String[] split = line.split("==");

                                        hashMap.put(split[0], split[1]);
                                    } catch (Exception e2) {
                                        continue;
                                    }
                                }
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                        }
                        if (hashMap.get(FilePath) != null) {
                            linenumber = Integer.parseInt(hashMap.get(FilePath));
                        } else {
                            linenumber = 0;
                        }

                        try {
                            inputStream = new FileInputStream(file);
                            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                            while (br.ready()) {
                                String line = br.readLine();
                                contentlist.add(line);
                            }
                            String content = contentlist.get(linenumber);
                            Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                            textArea2.setText(String.valueOf(words));
                            textArea1.setText(content);
                            textArea1.setCaretPosition(1);
                            label.setText("第 " + (linenumber + 1) + " 行");

                            for (Component component : JP3.getComponents()) {
                                if (component instanceof JButton) {
                                    JButton button = (JButton) component;
                                    flushBut(button);
                                }
                            }

                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        file = null;
                        textArea1.setText("");
                        textArea2.setText("");
                        JOptionPane.showMessageDialog(null, "所选文件不存在或所选路径为文件夹！", "Message", JOptionPane.INFORMATION_MESSAGE);
                    }

                }
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

        });
        entchoosebt.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                    beforeOperation();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    nextOperation();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

//        textArea1.addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                if(e.getWheelRotation()==1){
//                    System.out.println("wheel forward !");
//                }
//                if(e.getWheelRotation()==-2){
//                    System.out.println("wheel backward !");
//                }
//            }
//        });
        textArea1.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                    beforeOperation();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    nextOperation();
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    nextOperation();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        textArea2.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                    beforeOperation();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    nextOperation();
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    nextOperation();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        beforebt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 绑定到选择文件，先择文件事件
                if (e.getSource().equals(beforebt)) {// 判断触发方法的按钮是哪个
                    beforeOperation();
                }

            }
        });
        beforebt.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                    beforeOperation();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    nextOperation();
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    nextOperation();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        nextbt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 绑定到选择文件，先择文件事件
                if (e.getSource().equals(nextbt)) {// 判断触发方法的按钮是哪个
                    nextOperation();
                }

            }
        });
        nextbt.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                    beforeOperation();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    nextOperation();
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    nextOperation();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        otherclassbt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 绑定到选择文件，先择文件事件
                if (e.getSource().equals(otherclassbt)) {// 判断触发方法的按钮是哪个
                    writefile(otherclassbt.getName());
                    flushBut(otherclassbt);
                }

            }
        });
        otherclassbt.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                    beforeOperation();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    nextOperation();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private static void loadClassFile(String path) {
        String sysSeparator = File.separator;
        if (path.equals("")) path = "classification.txt";
        File file = new File(path);
        InputStream inputStream = null;
        try {
//            file = new File("/home/xavier/workspace/project/today/src/main/java/com/clps/classificationtools/classification.txt");

            if (!file.exists() || !file.isFile()) {
                String filename = "classification.txt";
                inputStream = TestV4.class.getResourceAsStream(filename); // 按指定路径从jar包加载资源文件
                if (inputStream == null) { // 加载指定包路径下的资源
                    if (!filename.startsWith(sysSeparator)) { // 改成相对路径再加载
                        filename = "/" + filename;
                    } else { // 改成绝对路径再加载
                        filename = filename.replaceFirst(sysSeparator, "");
                    }
                    inputStream = TestV4.class.getResourceAsStream(filename);
                }
            } else {
                inputStream = new FileInputStream(file);
            }

            if (inputStream != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                while (br.ready()) {
                    String line = (br.readLine()).trim();
                    if (line.equals("") || line.startsWith("#")) continue;
                    classificationlist.add(line);
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println("类别数量 : " + classificationlist.size());
    }

    private void beforeOperation() {
        if (file != null) {
            if (linenumber > 0) {
                linenumber--;
                String content = contentlist.get(linenumber);
                Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                textArea2.setText(String.valueOf(words));
                textArea1.setText(content);
                textArea1.setCaretPosition(1);
                label.setText("第 " + (linenumber + 1) + " 行");
                hashMap.put(FilePath, linenumber + "");
            } else {
                String content = contentlist.get(0);
                Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                textArea2.setText(String.valueOf(words));
                textArea1.setText(content);
                textArea1.setCaretPosition(1);
                label.setText("第 " + 1 + " 行");
                hashMap.put(FilePath, 0 + "");
                JOptionPane.showMessageDialog(null, "已经是第一条!", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            System.out.println("请先选择文件");
            JOptionPane.showMessageDialog(null, "请先选择文件", "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void nextOperation() {
        int count = contentlist.size();
        if (file != null) {
            if (linenumber < (count - 1)) {
                linenumber++;
                String content = contentlist.get(linenumber);
                Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                textArea2.setText(String.valueOf(words));
                textArea1.setText(content);
                textArea1.setCaretPosition(1);
                label.setText("第 " + (linenumber + 1) + " 行");
                hashMap.put(FilePath, linenumber + "");
            } else {
                String content = contentlist.get(count - 1);
                Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                textArea2.setText(String.valueOf(words));
                textArea1.setText(content);
                textArea1.setCaretPosition(1);
                label.setText("第 " + count + " 行");
                hashMap.put(FilePath, (count - 1) + "");
                JOptionPane.showMessageDialog(null, "已经是最后一条!", "Message", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            System.out.println("请先选择文件");
            JOptionPane.showMessageDialog(null, "请先选择文件", "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void creatButton() {
        int btnum = classificationlist.size();

        if (btnum > 0) {
            for (String classname : classificationlist) {
                if (index < 6) {
                    btx = index;
                } else {
                    bty = index / 6;
                    btx = index - (bty * 6);
                }
                JButton button;
//                System.out.println(btx+"--"+bty);
                button = new JButton();
                button.setActionCommand(classname);
                button.setAutoscrolls(true);
                button.setIconTextGap(6);
                button.setLabel(classname);
                button.setFocusable(false);
                button.setMaximumSize(new Dimension(120, 30));
                button.setMinimumSize(new Dimension(120, 30));
                button.setName(classname);
                button.setPreferredSize(new Dimension(120, 30));
                button.setText(classname);
                GridBagConstraints gbc;
                gbc = new GridBagConstraints();
                gbc.gridx = btx;
                gbc.gridy = bty;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.insets = new Insets(1, 1, 1, 1);
                JP3.add(button, gbc);

                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // 绑定到选择文件，先择文件事件
                        if (e.getSource().equals(button)) {// 判断触发方法的按钮是哪个
//                            System.out.println(button.getName());
                            writefile(button.getName());
                            flushBut(button);
                        }

                    }
                });

                button.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();//获取所按键盘的键盘编码
                        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {  //左键
                            beforeOperation();
                        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                            nextOperation();
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {

                    }
                });

                JP3.revalidate();
                index++;
            }
        }
    }

    private void writefile(String filename) {
        int count = contentlist.size();
        BufferedWriter out = null;

        try {
            if (file != null) {
                out = new BufferedWriter(new FileWriter(filedir.getPath() + "/" + filename + ".txt", true));

                if (linenumber < (count - 1)) {
                    String line = contentlist.get(linenumber);
                    out.write(line + "\n");
                    linenumber++;
                    String content = contentlist.get(linenumber);
                    Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                    textArea2.setText(String.valueOf(words));
                    textArea1.setText(content);
                    textArea1.setCaretPosition(1);
                    label.setText("第 " + (linenumber + 1) + " 行");
                    hashMap.put(FilePath, linenumber + "");
                } else {
                    String content = contentlist.get(count - 1);
                    Map<String, Integer> words = TextParser.getKeyWordsWithStatisticByMaxForwardMatch(WordDictionary.getIdfDictMap().keySet(), content);
                    textArea2.setText(String.valueOf(words));
                    textArea1.setText(content);
                    textArea1.setCaretPosition(1);
                    label.setText("第 " + count + " 行");
                    hashMap.put(FilePath, (count - 1) + "");
                    JOptionPane.showMessageDialog(null, "已经是最后一条!", "Message", JOptionPane.INFORMATION_MESSAGE);
                }

            } else {
                System.out.println("请先选择文件");
                JOptionPane.showMessageDialog(null, "请先选择文件", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException oe) {
            oe.printStackTrace();
        }
    }

    private int fileLines(String filename){
        if(filedir.getPath()==null||"".equals(filedir.getPath())){
            return 0;
        }
        String filePath = filedir.getPath() + "/" + filename + ".txt";
        File file = new File(filePath);
        if(file.exists()){
            //  获取文件行数
            try {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);
                lnr.skip(Long.MAX_VALUE);
                int lines = lnr.getLineNumber() ;
                lnr.close();
                return lines;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            return 0;
        }
    }

    private void flushBut(JButton button) {
        int lines = fileLines(button.getName());
        if(lines!=0)
            button.setText(button.getName() + "(" + lines + ")");
    }
    public static void main(String[] args) {
        WordDictionary.initial();
        String path = "";
        if (args.length == 1) path = args[0];

        loadClassFile(path);

        double lx = tk.getScreenSize().getWidth();

        double ly = tk.getScreenSize().getHeight();
        int x = (int) (lx / 3) - 150;
        int y = (int) (ly / 4) - 150;
        Image image = tk.createImage("tool3.jpg");
        /*image.gif是你的图标*/
        frame.setIconImage(image);

        frame.setName("rootframe");
        frame.setLocation(new Point(x, y));// 设定窗口出现位置
        frame.setContentPane(new TestV4().JP);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);


    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        JP = new JPanel();
        JP.setLayout(new BorderLayout(0, 0));
        JP.setMinimumSize(new Dimension(900, 700));
        JP.setPreferredSize(new Dimension(900, 700));
        JP1 = new JPanel();
        JP1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JP.add(JP1, BorderLayout.NORTH);
        filechoosebt = new JButton();
        filechoosebt.setText("文件选择");
        filechoosebt.setFocusable(false);
        JP1.add(filechoosebt);
        textField1 = new JTextField();
        textField1.setMinimumSize(new Dimension(264, 30));
        textField1.setPreferredSize(new Dimension(264, 30));
        JP1.add(textField1);
        entchoosebt = new JButton();
        entchoosebt.setFocusable(false);
        entchoosebt.setActionCommand("确认");
        entchoosebt.setLabel("确认");
        entchoosebt.setText("确认");
        JP1.add(entchoosebt);

        beforebt = new JButton();
        beforebt.setFocusable(false);
        beforebt.setLabel("上一条");
        beforebt.setMargin(new Insets(5, 5, 5, 5));
        beforebt.setMaximumSize(new Dimension(194, 30));
        beforebt.setMinimumSize(new Dimension(194, 30));
        beforebt.setName("上一条");
        beforebt.setPreferredSize(new Dimension(194, 30));
        beforebt.setText("上一条");
        JP1.add(beforebt);
        nextbt = new JButton();
        nextbt.setFocusable(false);
        nextbt.setLabel("下一条");
        nextbt.setMargin(new Insets(5, 5, 5, 5));
        nextbt.setMaximumSize(new Dimension(194, 30));
        nextbt.setMinimumSize(new Dimension(194, 30));
        nextbt.setName("下一条");
        nextbt.setPreferredSize(new Dimension(194, 30));
        nextbt.setText("下一条");
        JP1.add(nextbt);
        label = new JLabel();
        label.setRequestFocusEnabled(false);
        JP1.add(label);


        textArea1 = new JTextArea(10, 100);
        Font textArea1Font = this.$$$getFont$$$(null, -1, 18, textArea1.getFont());
        if (textArea1Font != null) textArea1.setFont(textArea1Font);
        textArea1.setLineWrap(true);
        textArea1.setBackground(Color.darkGray);
        textArea1.setForeground(Color.green);
        textArea1.setAutoscrolls(true);

        textArea1.setRequestFocusEnabled(true);
        textArea1.setEditable(false);

        textArea2 = new JTextArea(4, 100);
        if (textArea1Font != null) textArea2.setFont(textArea1Font);
        textArea2.setLineWrap(true);
        textArea2.setBackground(Color.darkGray);
        textArea2.setForeground(Color.PINK);
        textArea2.setRequestFocusEnabled(true);
        textArea2.setEditable(false);

        JP2 = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        JP2.setLayout(springLayout);
        JP2.setMinimumSize(new Dimension(500, 500));
        JP2.setPreferredSize(new Dimension(500, 500));
        JP2.setRequestFocusEnabled(false);
        JP2.setVisible(true);

        JP2_1 = new JScrollPane();
        JP2_1.setViewportView(textArea1);
        JP2_1.setRequestFocusEnabled(false);

        JP2_2 = new JScrollPane();
        JP2_2.setViewportView(textArea2);
        JP2_2.setRequestFocusEnabled(false);

        Spring st = Spring.constant(10);

        JP2.add(JP2_1);
        springLayout.putConstraint(SpringLayout.NORTH, JP2_1, st, SpringLayout.NORTH, JP2);
        springLayout.putConstraint(SpringLayout.WEST, JP2_1, st, SpringLayout.WEST, JP2);
        springLayout.putConstraint(SpringLayout.EAST, JP2_1, Spring.minus(st), SpringLayout.EAST, JP2);
        springLayout.putConstraint(SpringLayout.SOUTH, JP2_1, Spring.minus(st), SpringLayout.NORTH, JP2_2);


        JP2.add(JP2_2);
        springLayout.putConstraint(SpringLayout.WEST, JP2_2, st, SpringLayout.WEST, JP2);
        springLayout.putConstraint(SpringLayout.EAST, JP2_2, Spring.minus(st), SpringLayout.EAST, JP2);
        springLayout.putConstraint(SpringLayout.SOUTH, JP2_2, Spring.minus(st), SpringLayout.SOUTH, JP2);


        JP.add(JP2, BorderLayout.CENTER);


        JP3 = new JPanel();
        JP3.setLayout(new GridBagLayout());
        JP3.setAlignmentX(1.0f);
        JP3.setAlignmentY(1.0f);
        JP3.setAutoscrolls(false);
        JP3.setDoubleBuffered(true);
        JP3.setEnabled(true);
        JP3.setInheritsPopupMenu(false);
        JP3.setOpaque(true);
//        JP3.setPreferredSize(new Dimension(601, 136));
        JP3.setVisible(true);
        JP3.putClientProperty("html.disable", Boolean.TRUE);
        JP.add(JP3, BorderLayout.SOUTH);
        JP3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.CENTER, TitledBorder.BOTTOM));

        creatButton();

        if (index < 6) {
            btx = index;
        } else {
            bty = index / 6;
            btx = index - (bty * 6);
        }

        otherclassbt = new JButton();
        otherclassbt.setActionCommand("其他");
        otherclassbt.setAutoscrolls(true);
        otherclassbt.setIconTextGap(6);
        otherclassbt.setLabel("其他");
        otherclassbt.setMaximumSize(new Dimension(120, 30));
        otherclassbt.setMinimumSize(new Dimension(120, 30));
        otherclassbt.setName("其他");
        otherclassbt.setPreferredSize(new Dimension(120, 30));
        otherclassbt.setText("其他");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();

        gbc.gridx = btx;
        gbc.gridy = bty;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(1, 1, 1, 1);
        JP3.add(otherclassbt, gbc);
    }


    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return JP;
    }
}

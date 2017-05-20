package japanesecw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import jpl.Atom;
import jpl.Query;
import jpl.Term;
import jpl.Util;
import jpl.Variable;
import jpl.Compound;

public class JCWWindow extends JFrame {

    private JCWCanvas m_canvas;
    private String m_source_file;
    private ArrayList< ArrayList> m_columns_data;
    private ArrayList< ArrayList> m_rows_data;
    public boolean b_isFileOpened = false;

    public ArrayList< ArrayList> getColumnsData() {
        return m_columns_data;
    }

    public ArrayList< ArrayList> getRowsData() {
        return m_rows_data;
    }

    public boolean getIsFileOpened() {
        return b_isFileOpened;
    }

    public void setIsFileOpened(boolean flag) {
        b_isFileOpened = flag;
    }

    public JCWWindow(String str) {
        super(str);

        m_columns_data = new ArrayList();
        m_rows_data = new ArrayList();
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        Font menuFont = new Font("Arial", Font.PLAIN, 11);
        fileMenu.setFont(menuFont);
        menuBar.add(fileMenu);
        JMenu solveMenu = new JMenu("Задача");
        solveMenu.setFont(menuFont);
        menuBar.add(solveMenu);
        JMenuItem openItem = new JMenuItem("Открыть кроссворд...");
        openItem.setFont(menuFont);
        fileMenu.add(openItem);
        JMenuItem solveItem = new JMenuItem("Решить кроссворд!");
        solveItem.setFont(menuFont);
        solveMenu.add(solveItem);
        solveItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_canvas.clearField();
                m_canvas.repaint();
                Query q1 = new Query("consult", new Term[]{new Atom("japaneseCrosswordInProlog.pl")});
                System.out.println("-- consult " + (q1.query() ? "succeeded" : "failed"));
                ArrayList colList = JCWWindow.this.getColumnsData();
                ArrayList rowList = JCWWindow.this.getRowsData();
                ArrayList<ArrayList<Integer>> fieldList = m_canvas.getFilledArray();
                ArrayList<ArrayList<Integer>> fieldListCols = new ArrayList();
                ArrayList<ArrayList<Integer>> fieldListRows = new ArrayList();

                for (int i = 0; i < rowList.size(); i++) {
                    ArrayList<Integer> temp = new ArrayList();
                    for (int j = 0; j < colList.size(); j++) {
                        int curIndex = i * colList.size() + j;
                        temp.add(fieldList.get(curIndex).get(0));
                    }
                    fieldListRows.add(temp);
                }

                for (int i = 0; i < colList.size(); i++) {
                    ArrayList<Integer> temp = new ArrayList();
                    for (int j = 0; j < rowList.size(); j++) {
                        int curIndex = j * colList.size() + i;
                        temp.add(fieldList.get(curIndex).get(0));
                    }
                    fieldListCols.add(temp);
                }
                Variable X = new Variable("X");
                Variable Y = new Variable("Y");

                Term t1 = Util.textToTerm(colList.toString());
                Term t2 = Util.textToTerm(rowList.toString());
                Term t3 = Util.textToTerm(fieldListCols.toString());
                Term t4 = Util.textToTerm(fieldListRows.toString());

                Query q10 = new Query("очистить_бд");
                q10.open();
                q10.close();
                
                Query q2 = new Query("решить_кроссворд", new Term[]{t1, t2, t3, t4, X, Y});
                q2.open();

                Hashtable ht = q2.getSolution();
                Term[] arrX = ((Compound) ht.get("X")).toTermArray();
                int counter = 0;

                for (int i = 0; i < getRowsData().size(); i++) {
                    counter = 0;
                    for (int j = 0; j < getColumnsData().size(); j++) {
                        while (counter < arrX[i].toString().length()
                                && arrX[i].toString().charAt(counter) != '0'
                                && arrX[i].toString().charAt(counter) != '2'
                                && arrX[i].toString().charAt(counter) != '3') {
                            counter++;
                        }
                        if (counter < arrX[i].toString().length()
                                && arrX[i].toString().charAt(counter) == '2') {
                            m_canvas.fillCell2(j, i);
                            counter++;
                        } else if (counter < arrX[i].toString().length()
                                && arrX[i].toString().charAt(counter) == '3') {
                            m_canvas.fillCell3(j, i);
                            counter++;
                        }else if (counter < arrX[i].toString().length()
                                && arrX[i].toString().charAt(counter) == '0'){
                            counter++;
                        }

                    }
                }
                m_canvas.repaint();
                q2.close();
            }
        });

        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Japanese crossword files (*.jcw)", "jcw");
                chooser.setCurrentDirectory(null);
                chooser.setFileFilter(filter);
                chooser.addChoosableFileFilter(filter);

                int returnVal = chooser.showOpenDialog(JCWWindow.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File choosed = chooser.getSelectedFile();
                    m_source_file = choosed.getPath();
                    setIsFileOpened(true);
                    try {
                        m_columns_data.clear();
                        m_rows_data.clear();
                        ArrayList<String> listOfStrings = readFile();
                        ArrayList<Integer> listOfInteger = proceedToIntegerData(listOfStrings);
                        m_canvas.clearField();
                        m_canvas.repaint();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(JCWWindow.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(JCWWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        this.setJMenuBar(menuBar);
        m_canvas = new JCWCanvas(this);
        this.add(m_canvas);
        this.setLocationRelativeTo(null);
        this.setSize(400, 400);
        this.setMinimumSize(new Dimension(150, 150));
        this.setMaximumSize(new Dimension(1280, 1024));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public ArrayList<Integer> proceedToIntegerData(ArrayList<String> listOfStrings) {
        ArrayList<Integer> result = new ArrayList();
        int columnNum = -1;
        int rowNum = -1;
        rowNum = Integer.parseInt(listOfStrings.get(0));
        m_canvas.setINum(rowNum);
        for (int i = 1; i <= rowNum; i++) {
            String[] tempStrList = listOfStrings.get(i).split(" ");
            ArrayList<Integer> tempIntList = new ArrayList();
            for (int j = 0; j < tempStrList.length; j++) {
                tempIntList.add(Integer.parseInt(tempStrList[j]));
            }
            m_rows_data.add(tempIntList);
        }
        columnNum = Integer.parseInt(listOfStrings.get(rowNum + 1));
        m_canvas.setJNum(columnNum);
        for (int i = rowNum + 2; i < rowNum + columnNum + 2; i++) {
            String[] tempStrList = listOfStrings.get(i).split(" ");
            ArrayList<Integer> tempIntList = new ArrayList();
            for (int j = 0; j < tempStrList.length; j++) {
                tempIntList.add(Integer.parseInt(tempStrList[j]));
            }
            m_columns_data.add(tempIntList);
        }
        System.out.println(m_rows_data);
        System.out.println(m_columns_data);
        return result;
    }

    public ArrayList<String> readFile() throws FileNotFoundException, IOException {
        File file;
        FileReader freader;
        LineNumberReader lnreader;
        ArrayList<String> result = new ArrayList();
        file = new File(m_source_file);
        freader = new FileReader(file);
        lnreader = new LineNumberReader(freader);
        try {
            String line = "";
            while ((line = lnreader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(JCWWindow.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            freader.close();
            lnreader.close();
        }
        return result;
    }
}

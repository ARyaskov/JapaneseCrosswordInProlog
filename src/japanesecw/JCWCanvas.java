package japanesecw;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class JCWCanvas extends JLabel {
    private int m_grid_factor = 16;
    private int m_start_delta = 100;
    private int m_i_num = 100;
    private int m_j_num = 100;
    private JCWWindow m_window;
    private ArrayList<ArrayList> m_columns_data;
    private ArrayList<ArrayList> m_rows_data;
    private int[][] m_filled = new int[m_i_num][m_j_num];

    public JCWCanvas(JCWWindow win) {
        super();

        for (int i = 0; i < m_filled.length; i++) {
            for (int j = 0; j < m_filled[i].length; j++) {
                m_filled[i][j] = 0;
            }
        }
        setBackground(Color.WHITE);
        m_window = win;
        m_columns_data = win.getColumnsData();
        m_rows_data = win.getRowsData();
    }

    public ArrayList<ArrayList<Integer>> getFilledArray() {
        ArrayList<ArrayList<Integer>> result = new ArrayList();
        for (int i = 0; i < getINum(); i++) {
            for (int j = 0; j < getJNum(); j++) {
                ArrayList<Integer> temp = new ArrayList();
                temp.add(Integer.valueOf(getValFromFilled(i, j)));
                result.add(temp);
            }
        }
        return result;
    }

    public int getValFromFilled(int col, int row) {
        return m_filled[row][col];
    }

    public void setINum(int i) {
        m_i_num = i;
    }

    public void setJNum(int j) {
        m_j_num = j;
    }

    public int getINum() {
        return m_i_num;
    }

    public int getJNum() {
        return m_j_num;
    }

    public void fillCell2(int x, int y) {
        m_filled[x][y] = 2;

    }

    public void fillCell3(int x, int y) {
        m_filled[x][y] = 3;

    }

    public void clearField() {
        for (int i = 0; i < getINum(); i++) {
            for (int j = 0; j < getJNum(); j++) {
                m_filled[j][i] = 0;
            }
        }


    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        if (m_window.getIsFileOpened()) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            m_window.setSize((m_j_num + 1) * m_grid_factor + m_start_delta, (m_i_num + 4) * m_grid_factor + m_start_delta);
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
            g2.setColor(Color.cyan);
            Stroke stroke = new BasicStroke(5);
            Stroke oldstroke = g2.getStroke();
            g2.setStroke(stroke);
            g2.drawRect(0, 0, this.getWidth(), this.getHeight());
            g2.setStroke(oldstroke);
            drawLines(g2);
        }
    }

    public void drawLines(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        int w = m_start_delta + m_grid_factor * m_j_num;
        int h = m_start_delta + m_grid_factor * m_i_num;
        boolean isDrawStrings = true;

        for (int i = 0; i < m_i_num; i++) {
            // горизонтальные линии
            g2.drawLine(0, i * m_grid_factor + m_start_delta, w, i * m_grid_factor + m_start_delta);
            for (int j = 0; j < m_j_num; j++) {
                g2.drawLine(j * m_grid_factor + m_start_delta, 0, j * m_grid_factor + m_start_delta, h);
                if (m_filled[j][i] == 2) {
                    Color prevColor = g2.getColor();
                    g2.fillRect(j * m_grid_factor + m_start_delta, i * m_grid_factor + m_start_delta, m_grid_factor - 1, m_grid_factor - 1);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(j * m_grid_factor + m_start_delta, i * m_grid_factor + m_start_delta, m_grid_factor, m_grid_factor);
                    g2.setColor(prevColor);
                } else if (m_filled[j][i] == 3) {
                    Color prevColor = g2.getColor();
                    g2.setColor(Color.RED);
                    g2.fillRect(j * m_grid_factor + m_start_delta, i * m_grid_factor + m_start_delta, m_grid_factor - 1, m_grid_factor - 1);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(j * m_grid_factor + m_start_delta, i * m_grid_factor + m_start_delta, m_grid_factor, m_grid_factor);
                    g2.setColor(prevColor);
                } else {
                    g2.drawRect(j * m_grid_factor + m_start_delta, i * m_grid_factor + m_start_delta, m_grid_factor, m_grid_factor);
                }
                if (j == m_j_num - 1) {
                    g2.drawLine((j + 1) * m_grid_factor + m_start_delta, 0, (j + 1) * m_grid_factor + m_start_delta, h);
                }
            }
            if (i == m_i_num - 1) {
                g2.drawLine(0, (i + 1) * m_grid_factor + m_start_delta, w, (i + 1) * m_grid_factor + m_start_delta);
            }
        }
        for (int i = 0; i < m_i_num; i++) {
            for (int j = 0; j < m_j_num; j++) {
                if (m_rows_data.size() > 0) {
                    int size = m_rows_data.get(i).size();
                    for (int m = 0; m < size; m++) {
                        g2.drawString(((Integer) m_rows_data.get(i).get(m)).toString(), m * m_grid_factor + 5, (i + 1) * m_grid_factor + m_start_delta - 3);
                    }
                    size = m_columns_data.get(j).size();
                    for (int k = 0; k < size; k++) {
                        if (((Integer) m_columns_data.get(j).get(k)).toString().length() > 1) {
                            g2.drawString(((Integer) m_columns_data.get(j).get(k)).toString(), j * m_grid_factor + m_start_delta + (m_grid_factor / 2) - 5, (k) * m_grid_factor + 20);
                        } else {
                            g2.drawString(((Integer) m_columns_data.get(j).get(k)).toString(), j * m_grid_factor + m_start_delta + (m_grid_factor / 2), (k) * m_grid_factor + 20);
                        }
                    }
                }

                if (i > m_rows_data.size()) {
                    isDrawStrings = false;
                }
                if (j > m_columns_data.size()) {
                    isDrawStrings = false;
                }
            }
        }
    }
}

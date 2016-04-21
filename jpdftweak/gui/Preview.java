package jpdftweak.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;



public class Preview extends JPanel {

    private int height;
    private DefaultListModel listModel;
    private JList<JLabel> list;
    private Dimension dim;
    private JScrollPane jsp;
    private final float mmPerPixel = (25.4f/Toolkit.getDefaultToolkit().getScreenResolution());
    private float scaleValue;


    public Preview(Dimension d)  {
        super(new FormLayout("f:p:g", "f:p:g"));
        dim=d;
        CellConstraints cc = new CellConstraints();

        height = d.height;

        listModel = new DefaultListModel();
        list = new JList<>(listModel);
        myRenderer r = new myRenderer();
       // r.setHorizontalAlignment(JLabel.CENTER);
        list.setCellRenderer(r);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setBackground(Color.gray);
        //list.setVisibleRowCount(-1);
        jsp = new JScrollPane(list);

///////
        createRuler(jsp);
      //////

        jsp.setBackground(Color.gray);
        jsp.getVerticalScrollBar().setUnitIncrement(8);
        jsp.setPreferredSize(d);


        add(jsp, cc.xy(1,1));

    }

    private void createRuler(JScrollPane scrollPane) {

        JLabel[] corners = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            corners[i] = new JLabel();
            corners[i].setBackground(Color.lightGray);
            corners[i].setOpaque(true);
        }

//        System.out.println(mmPerPixel);
//        System.out.println(Toolkit.getDefaultToolkit().getScreenResolution());

        JLabel rowheader = new JLabel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Rectangle rect = g.getClipBounds();
                for (int i = 10 - (rect.y % 10); i < rect.height; i += 10) {
                    g.drawLine(0, rect.y + (int)(i/mmPerPixel), 3, rect.y + (int)(i/mmPerPixel));
                    g.drawString("" + (rect.y + i), 6, rect.y + (int)(i/mmPerPixel) + 3);
                }
            }

            public Dimension getPreferredSize() {
                return new Dimension(25, (int) dim.getHeight()
                );
            }
        };
        rowheader.setBackground(Color.lightGray);
        rowheader.setOpaque(true);


        JLabel columnheader = new JLabel() {

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Rectangle r = g.getClipBounds();
                for (int i = 10 - (r.x % 10); i < r.width; i += 10) {
                    g.drawLine(r.x + (int)(i/mmPerPixel), 0, r.x + (int)(i/mmPerPixel), 3);
                    g.drawString("" + (r.x + i), r.x + (int)(i/mmPerPixel) - 10, 16);
                }
            }

            public Dimension getPreferredSize() {
                return new Dimension((int) dim.getWidth(),
                        25);
            }
        };
        columnheader.setBackground(Color.lightGray);
        columnheader.setOpaque(true);

        scrollPane.setRowHeaderView(rowheader);
        scrollPane.setColumnHeaderView(columnheader);
        scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, corners[0]);
        scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, corners[1]);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corners[2]);
        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corners[3]);
    }

    private void updateRuler(JScrollPane scrollPane) {
        JLabel rowheader = new JLabel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Rectangle rect = g.getClipBounds();
                for (int i = 10 - (rect.y % 10); i < rect.height; i += 10) {
                    g.drawLine(0, rect.y + (int)(i*scaleValue/mmPerPixel), 3, rect.y + (int)(i*scaleValue/mmPerPixel));
                    g.drawString("" + (rect.y + i), 6, rect.y + (int)(i*scaleValue/mmPerPixel) + 3);
                }
            }

            public Dimension getPreferredSize() {
                return new Dimension(25, (int) dim.getHeight()
                );
            }
        };
        rowheader.setBackground(Color.lightGray);
        rowheader.setOpaque(true);


        JLabel columnheader = new JLabel() {

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Rectangle r = g.getClipBounds();
                for (int i = 10 - (r.x % 10); i < r.width; i += 10) {
                    g.drawLine(r.x + (int)(i*scaleValue/mmPerPixel), 0, r.x + (int)(i*scaleValue/mmPerPixel), 3);
                    g.drawString("" + (r.x + i), r.x + (int)(i*scaleValue/mmPerPixel) - 10, 16);
                }
            }

            public Dimension getPreferredSize() {
                return new Dimension((int) dim.getWidth(),
                        25);
            }
        };
        columnheader.setBackground(Color.lightGray);
        columnheader.setOpaque(true);

        scrollPane.setRowHeaderView(rowheader);
        scrollPane.setColumnHeaderView(columnheader);
    }

    public void updatePreview(ByteBuffer buf, float dpi) {

        clearPreview();

            float dpiMod = dpi/100;
            PDFFile pdffile = null;
            try {
                pdffile = new PDFFile(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image img;
            for (int i = 0; i < pdffile.getNumPages(); i++) {
                PDFPage page = pdffile.getPage(i+1);

                int rwidth = (int) (page.getBBox().getWidth());
                int rheight = (int) (page.getBBox().getHeight());
                Rectangle rect =
                        new Rectangle(0, 0, rwidth, rheight);

                int width;
                int height;
                if (rheight >= rwidth) {
                     height = jsp.getHeight() - 25;
                     width = (int) (page.getAspectRatio() * height);
                } else {
                    width =jsp.getWidth() - 25;
                    height = (int) (width/page.getAspectRatio());

                }

                img = page.getImage(width, height, rect, null, true, true);
                img = img.getScaledInstance((int)(width*dpiMod), (int)(height*dpiMod), Image.SCALE_DEFAULT);
                img = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
                listModel.addElement(new JLabel(new ImageIcon(img)));

                if(i==0) {
                    scaleValue = (width*1f)/(rwidth*1f);
                }
            }
        updateRuler(jsp);

    }
    
    public void clearPreview() {
        listModel.clear();
    }

    public static void main(final String[] args) {
       JFrame frame = new JFrame("Yolo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Preview(new Dimension(500, 700)));
        frame.pack();
        frame.setVisible(true);
    }

    public class myRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,boolean isSelected, boolean cellHasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(((JLabel) value).getIcon());
            label.setText(null);
            return label;
        }
    }
}

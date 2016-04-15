package jpdftweak.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;



public class Preview extends JPanel {

    private int height;
    private DefaultListModel listModel;
    private JList<JLabel> list;


    public Preview(Dimension d)  {
        super(new FormLayout("f:p:g", "f:p:g"));
        CellConstraints cc = new CellConstraints();

        height = d.height;

        listModel = new DefaultListModel();
        list = new JList<>(listModel);

        list.setCellRenderer(new myRenderer());
        list.setLayoutOrientation(JList.VERTICAL);
        list.setBackground(Color.gray);
        //list.setVisibleRowCount(-1);
        JScrollPane jsp = new JScrollPane(list);
        jsp.setBackground(Color.gray);
        jsp.getVerticalScrollBar().setUnitIncrement(8);
        jsp.setPreferredSize(d);


        add(jsp, cc.xyw(1,1,1));

    }

    public void updatePreview(ByteBuffer buf, float n) {

        clearPreview();

            float mod = n/100;
            PDFFile pdffile = null;
            try {
                pdffile = new PDFFile(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image img;
            for (int i = 0; i < pdffile.getNumPages(); i++) {
                PDFPage page = pdffile.getPage(i+1);
                Rectangle rect =
                        new Rectangle(0, 0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());
                int height = (int)(page.getBBox().getHeight()*mod);

                int width = (int)(page.getAspectRatio()*height);

                img = page.getImage(width, height, rect, null, true, true);
                listModel.addElement(new JLabel(new ImageIcon(img)));
            }

    }

    public void updatePreview(ByteBuffer buf) {

        clearPreview();


            PDFFile pdffile = null;
            try {
                pdffile = new PDFFile(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image img;
            for (int i = 0; i < pdffile.getNumPages(); i++) {
                PDFPage page = pdffile.getPage(i+1);
                int width = (int)(page.getAspectRatio()*height);

                Rectangle rect =
                        new Rectangle(0, 0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());

                img = page.getImage(width, height, rect, null, true, true);
                listModel.addElement(new JLabel(new ImageIcon(img)));
            }

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

package jpdftweak.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.TableComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPageLabels.PdfPageLabelFormat;
import jpdftweak.gui.dialogs.OutputProgressDialog;

public class PageNumberTab extends Tab {

	private JButton load;
	private TableComponent pageNumberRanges;
	private JCheckBox changePageNumbers;

	private static final String[] NUMBER_STYLES = new String[] {
		"1, 2, 3", "I, II, III", "i, ii, iii", 
		"A, B, C", "a, b, c", "Empty"};

	public PageNumberTab(MainForm mf) {
		super(new FormLayout("f:p:g, f:p", "f:p, f:p, f:p:g"));
		CellConstraints cc = new CellConstraints();
		add(changePageNumbers = new JCheckBox("Change page numbers"), cc.xy(1, 1));
		changePageNumbers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnabledState();
			}
		});
		add(load = new JButton("Load from document"), cc.xy(2, 1));
		add(pageNumberRanges = buildPageNumberRanges(), cc.xyw(1, 3, 2));
		load.addActionListener(new PageNumberLoadAction(mf, pageNumberRanges));
		updateEnabledState();
	}

	public static TableComponent buildPageNumberRanges() {
		TableComponent result = new TableComponent(new String[] {"Start Page", "Style",  "Prefix", "Logical Page"},
				new Class[]{ Integer.class, String.class, String.class, Integer.class},
				new Object[]{1, NUMBER_STYLES[0], "", 1});
		JComboBox styleValues = new JComboBox(NUMBER_STYLES);
		result.getTable().getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(styleValues));
		return result;
	}

	protected void updateEnabledState() {
		load.setEnabled(changePageNumbers.isSelected());
		pageNumberRanges.setEnabled(changePageNumbers.isSelected());
	}

	@Override
	public String getTabName() {
		return "Page Numbers";
	}

	@Override
	public void checkRun() throws IOException {
		pageNumberRanges.checkRun("page number");
	}
	
	@Override
	public PdfTweak run(PdfTweak tweak, OutputProgressDialog outDialog) throws IOException, DocumentException {
                outDialog.updateTweaksProgress(getTabName());
		if (changePageNumbers.isSelected()) {
			updatePageNumberRanges(tweak, pageNumberRanges, outDialog);
		}
		return tweak;
	}

	public static void updatePageNumberRanges(PdfTweak tweak,
			TableComponent pageNumberRanges, OutputProgressDialog outDialog) throws DocumentException,
			IOException {
		PdfPageLabelFormat[] fmts = new PdfPageLabelFormat[pageNumberRanges.getRowCount()];
		for (int i = 0; i < fmts.length; i++) {
			Object[] row = pageNumberRanges.getRow(i);
			int nstyle = Arrays.asList(NUMBER_STYLES).indexOf(row[1]);
			if (nstyle == -1) nstyle = 0;
			fmts[i] = new PdfPageLabelFormat((Integer)row[0], nstyle, (String)row[2], (Integer)row[3]);
		}
		tweak.setPageNumbers(fmts, outDialog);
	}

	public static class PageNumberLoadAction implements ActionListener {
		
		private final TableComponent pageNumberRanges;
		private final MainForm mainForm;
		
		public PageNumberLoadAction(MainForm mainForm, TableComponent pageNumberRanges) {
			this.mainForm = mainForm;
			this.pageNumberRanges = pageNumberRanges;
		}
		
		public void actionPerformed(ActionEvent e) {
			pageNumberRanges.clear();
			if (mainForm.getInputFile() == null)
				return;
			PdfPageLabelFormat[] lbls = mainForm.getInputFile().getPageLabels();
			if (lbls != null) {
				for (PdfPageLabelFormat lbl : lbls) {
					pageNumberRanges.addRow(lbl.physicalPage,
							NUMBER_STYLES[lbl.numberStyle], lbl.prefix,
							lbl.logicalPage);
				}
			}
		}
	}
}

package misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

/**
 * 
 * @author Mini&Le
 *resposible to take care of freeze of all windows
 */
public class FreezeHandle extends JTableHeader{
	private JScrollPane scrollPane;
	private JLabel freezeColumn;
	private boolean added;
	private int col=-1;
	private int division;
	
	public FreezeHandle( final JTable table, JScrollPane scrollPane){
		super(table.getTableHeader().getColumnModel());
		this.table=table;
		this.scrollPane=scrollPane;
		table.setTableHeader(this);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		freezeColumn= new JLabel(){
			@Override
			public void paintComponent(Graphics g) {
				Rectangle r = table.getBounds();
				if (division > 0 && r.height>0) {
					table.invalidate();
					table.validate();
					Rectangle visibleRect = table.getVisibleRect();
					BufferedImage image = new BufferedImage(division, r.height,
							BufferedImage.TYPE_INT_ARGB);
					Graphics g2 = image.getGraphics();
					g2.setClip(0, visibleRect.y, division,
							table.getBounds().height);
					g2.setColor(Color.WHITE);
					g2.fillRect(0, 0, division, table.getBounds().height); 
					table.paint(g2);
					g.drawImage(image, 0, 0, division,
							table.getBounds().height, 0, visibleRect.y,
							division, visibleRect.y + table.getBounds().height,
							null);
					g.setColor(Color.BLACK);
					for (int i = 0; i < visibleRect.y
							+ table.getBounds().height; i += 8) {
						g.drawLine(division - 1, i, division - 1, i + 4);
						g.drawLine(division - 2, i, division - 2, i + 4);
					}
					g2.dispose();
				}
			}
		};
	}
	
	public void setBoundsOnFrozenColumns() {
		if (col >= 0) {
			division = table.getCellRect(1, col, true).x
					+ table.getCellRect(1, col, true).width;
			int limit = scrollPane.getBounds().width
					- scrollPane.getVerticalScrollBar().getBounds().width
					- 2;
			division = Math.min(division, limit);
			JLayeredPane pane = table.getRootPane().getLayeredPane();
			Point p = scrollPane.getLocationOnScreen();
			SwingUtilities.convertPointFromScreen(p, pane);
			Rectangle scrollPaneBounds = scrollPane.getBounds();
			int headerHeight = table.getTableHeader().getBounds().height + 1;
			int hScrollHeight = (scrollPane.getHorizontalScrollBar()
					.isVisible()) ? scrollPane.getHorizontalScrollBar()
					.getBounds().height : 0;
					freezeColumn.setBounds(p.x + 1, p.y + headerHeight, division,
					scrollPaneBounds.height - headerHeight - hScrollHeight
							- 2);
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if (division > 0) {
			Rectangle r = getVisibleRect();
			BufferedImage image = new BufferedImage(division, r.height,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g2 = image.getGraphics();
			g2.setClip(0, 0, division, r.height);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, division, r.height);
			super.paint(g2);
			g.drawImage(image, r.x, r.y, division, r.height, null);
			g2.dispose();
		}
	}
	
	
	public void freezeit() {
		col = 1;
		JLayeredPane pane = table.getRootPane().getLayeredPane();
		if (added) {
			pane.remove(freezeColumn);
		} else {
			scrollPane.addComponentListener(new ComponentListener() {

				public void componentHidden(ComponentEvent arg0) {
					// TODO Auto-generated method stub

				}

				public void componentMoved(ComponentEvent arg0) {
					// TODO Auto-generated method stub

				}

				public void componentResized(ComponentEvent arg0) {
					setBoundsOnFrozenColumns();

				}

				public void componentShown(ComponentEvent arg0) {
					// TODO Auto-generated method stub

				}

			});
		}
		pane.add(freezeColumn, JLayeredPane.POPUP_LAYER);
		setBoundsOnFrozenColumns();
		added=true;
		freezeColumn.setVisible(true);
	}
	
	public void freeze(){
		
		if (division<=0){
			freezeit();
		}else{
			freezeColumn.setVisible(false);
			division = -1;
			col = -1;
		}
	}

}

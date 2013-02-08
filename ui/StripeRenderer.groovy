import java.awt.*;
import javax.swing.*;

class StripeRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object pkg,
            int index, boolean isSelected, boolean cellHasFocus) {
        /*Label label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);*/
        def emptyText = "<html><font size=\"4\">&nbsp;</font></html>"
        def text = "<html><p><font size=\"4\" color=\"${isSelected ? 'black' : 'white'}\"><b>${isSelected ? '<i>' : ''}${pkg.name}${isSelected ? '</i>' : ''}</b></font></p><p>${pkg.type}</p></html>"

        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.LINE_AXIS));
        item.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(144, 186, 222)))

        GradientLabel leftBorder = new GradientLabel(emptyText)
        leftBorder.start = leftBorder.end = new Color(25, 88, 183)
        leftBorder.setPreferredSize(new Dimension(10, 0))
        leftBorder.setBorder(BorderFactory.createEmptyBorder(12,0,12,0))

        GradientLabel textBlock = new GradientLabel(text)
        textBlock.start = textBlock.end = new Color(33, 117, 188)
        textBlock.setPreferredSize(new Dimension(340, 0))
        textBlock.setBorder(BorderFactory.createEmptyBorder(12,10,12,0))

        GradientLabel rightBorder = new GradientLabel(emptyText)
        rightBorder.start = rightBorder.end = new Color(80, 143, 196)
        rightBorder.setPreferredSize(new Dimension(10, 0))
        rightBorder.setBorder(BorderFactory.createEmptyBorder(12,0,12,0))

        if(isSelected) {
            leftBorder.start = leftBorder.end = new Color(28, 100, 209)
            /*textBlock.start = textBlock.end = new Color(37, 134, 215)
            rightBorder.start = rightBorder.end = new Color(37, 134, 215)*/
            textBlock.start = textBlock.end = new Color(157, 195, 224)
            rightBorder.start = rightBorder.end = new Color(255, 255, 255)
        }

        item.add(leftBorder)
        item.add(textBlock)
        item.add(rightBorder)

        return item
    }
}
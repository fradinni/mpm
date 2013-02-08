import java.awt.*;
import javax.swing.*;

class StripeRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object pkg,
            int index, boolean isSelected, boolean cellHasFocus) {
        /*Label label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);*/
        
        /*
        if(isSelected) {
            leftBorder.start = leftBorder.end = new Color(28, 100, 209)
            textBlock.start = textBlock.end = new Color(37, 134, 215)
            rightBorder.start = rightBorder.end = new Color(37, 134, 215)
            textBlock.start = textBlock.end = new Color(157, 195, 224)
            rightBorder.start = rightBorder.end = new Color(255, 255, 255)
        }
        */


        def colors = [
            "modloader": [
                "default": [
                    "text": "#dd0404",
                    "left": new Color(221, 4, 4), /* RED */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": "#dd0404",
                    "left": new Color(221, 4, 4) /* RED */,
                    "middle": new Color(234, 234, 234),
                    "right" : new Color(234, 234, 234)
                ],
                "focus": [

                ]
            ],
            "mod": [
                "default": [
                    "text": "#2c7fcc",
                    "left": new Color(44, 127, 204), /* BLUE */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": "#2c7fcc",
                    "left": new Color(44, 127, 204), /* BLUE */,
                    "middle": new Color(234, 234, 234),
                    "right" : new Color(234, 234, 234)
                ]
            ],
            "texturepack": [
                "default": [
                    "text": new Color(58, 201, 75),
                    "left": new Color(58, 201, 75), /* GREEN */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": new Color(58, 201, 75),
                    "left": new Color(58, 201, 75), /* GREEN */,
                    "middle": new Color(242, 242, 242),
                    "right" : new Color(242, 242, 242)
                ]
            ]

        ]

        def defaultFontColor = colors."${pkg.type}"."default"."text"
        def selectedFontColor = colors."${pkg.type}"."selected"."text" ?: defaultFontColor

        def defaultLeftColor = colors."${pkg.type}"."default"."left"
        def selectedLeftColor = colors."${pkg.type}"."selected"."left" ?: defaultLeftColor

        def defaultMiddleColor = colors."${pkg.type}"."default"."middle"
        def selectedMiddleColor = colors."${pkg.type}"."selected"."middle" ?: defaultMiddleColor

        def defaultRightColor = colors."${pkg.type}"."default"."right"
        def selectedRightColor = colors."${pkg.type}"."selected"."right" ?: defaultRightColor

        def defaultBorderColor = colors."${pkg.type}"."default"."border"?."color"
        def selectedBorderColor = colors."${pkg.type}"."selected"."border"?."color" ?: defaultBorderColor

        def emptyText = "<html><font size=\"4\">&nbsp;</font></html>"
        def text = "<html><p><font size=\"4\" color=\"${isSelected ? defaultFontColor : selectedFontColor}\"><b>${isSelected ? '<i>' : ''}${pkg.name} ${pkg.version}${isSelected ? '</i>' : ''}</b></font></p><p>${pkg.type}</p></html>"

        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.LINE_AXIS));
        

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
            if(selectedBorderColor) item.setBorder(BorderFactory.createMatteBorder(1,0,0,0, selectedBorderColor))
            leftBorder.start = leftBorder.end = selectedLeftColor
            textBlock.start = textBlock.end = selectedMiddleColor
            rightBorder.start = rightBorder.end = selectedRightColor
        } else {
            if(selectedBorderColor) item.setBorder(BorderFactory.createMatteBorder(1,0,0,0, defaultBorderColor))
            leftBorder.start = leftBorder.end = defaultLeftColor
            textBlock.start = textBlock.end = defaultMiddleColor
            rightBorder.start = rightBorder.end = defaultRightColor
        }

        item.add(leftBorder)
        item.add(textBlock)
        item.add(rightBorder)

        return item
    }
}
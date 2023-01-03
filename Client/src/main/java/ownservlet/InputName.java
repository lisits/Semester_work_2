//package ownservlet;
//
//import lombok.Data;
//
//import javax.swing.*;
//import java.awt.*;
//
//@Data
//public class InputName extends JPanel {
//    private JButton button;
//
//    public InputName() {
//        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//
//        this.add(Box.createVerticalStrut(DIMENSION.height/20));
//        JLabel label = new JLabel("Drivel");
//        label.setFont(titleFont);
//        label.setAlignmentX(Component.CENTER_ALIGNMENT);
//        this.add(label);
//
//        this.add(Box.createVerticalStrut(DIMENSION.height/10));
//
//        JTextField textField = new HintTextField("Nickname");
//        Dimension tfSize = new Dimension(DIMENSION.width/4, DIMENSION.height/20);
//        textField.setMaximumSize(tfSize);
//        textField.setMinimumSize(tfSize);
//        textField.setBackground(GuiConst.COLOR);
//        textField.setBorder(BLACK_BORDER);
//        textField.addKeyListener(new InputNameListener(textField));
//        Font hintFont = Loader.loadFont("default.otf").deriveFont(DIMENSION.height/30f);
//        textField.setFont(hintFont);
//        this.add(textField, Component.CENTER_ALIGNMENT);
//
//        this.add(Box.createVerticalStrut(DIMENSION.height/20));
//        Dimension size = new Dimension(DIMENSION.width/10, DIMENSION.height/20);
//        JLabel fake = new JLabel();
//        fake.setMinimumSize(size);
//        fake.setMaximumSize(size);
//        this.add(fake, Component.CENTER_ALIGNMENT);
//
//        button = new JButton("Ввести");
//        button.setBackground(GuiConst.COLOR);
//        Dimension bSize = new Dimension(DIMENSION.width/10, DIMENSION.height/25);
//        button.setFont(hintFont);
//        button.setMinimumSize(bSize);
//        button.setMaximumSize(bSize);
//        button.setBorder(BLACK_BORDER);
//        button.addActionListener(new InputNameListener(textField));
//        this.add(button);
//
//        this.setBorder(BLACK_BORDER);
//        Dimension thisSize = new Dimension(DIMENSION.width/2, DIMENSION.height - DIMENSION.height/3);
//        this.setMinimumSize(thisSize);
//        this.setMaximumSize(thisSize);
//    }
//
//    public void showInvalidNameTip(){
//        JLabel jLabel = new JLabel("Имя занято");
//        Dimension size = new Dimension(DIMENSION.width/10, DIMENSION.height/20);
//        jLabel.setMinimumSize(size);
//        jLabel.setMaximumSize(size);
//        jLabel.setForeground(Color.RED);
//        this.remove(4);
//        this.add(jLabel, Component.RIGHT_ALIGNMENT, 4);
//
//        this.validate();
//        this.repaint();
//    }
//}
//

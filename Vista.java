/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package covid19app;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import weka.classifiers.functions.LinearRegression;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Daniel Andrés Bossio Pérez
 * @código 0221610003
 */
public class Vista extends javax.swing.JFrame {

    //Se tiene como variables globales los sets de contagios y muertes, y el gráfico
    private Instances contagios, muertes;
    private JFreeChart grafico;
    private boolean grafC, grafM;
    /**
     * Creates new form Vista
     */
    public Vista() {
        //inicialización de componentes 
        initComponents();
        grafC= false;
        grafM= false;
        //Apertura del archivo donde están los datos e inicialización de las listas de contagios y de muertes
        BufferedReader lec;
        String rutaC = "."+File.separator+"covid19casos.csv", linea;
        FastVector fv1 = new FastVector(2), fv2 = new FastVector(2);
        fv1.addElement(new Attribute("Dia"));
        fv1.addElement(new Attribute("Contagios"));
        fv2.addElement(new Attribute("Dia"));
        fv2.addElement(new Attribute("Muertes"));
        contagios=new Instances("Listado Contagios", fv1, 100);
        muertes = new Instances("Listado Muertes", fv2, 100);
        //Variables auxiliares para leer la información del archivo csv
        Instance c, m;
        Attribute a;
        String[] vals;
        double dia;
        
        try{
            //Se lee el archivo con BufferedReader y se guarda la información
            lec = new BufferedReader(new FileReader(rutaC));
            linea = lec.readLine();
            
            while(linea!=null){
                //La coma es el separador de los datos
                vals = linea.split(",");
                
                //Se crea una instancia de contagios en un día
                c = new Instance(2);
                c.setValue((Attribute)fv1.elementAt(0), Double.parseDouble(vals[0]));
                c.setValue((Attribute)fv1.elementAt(1), Double.parseDouble(vals[1]));
                this.contagios.add(c);
                
                //Se crea una instancia de muuertes en un día
                m = new Instance(2);
                m.setValue((Attribute)fv2.elementAt(0), Double.parseDouble(vals[0]));
                m.setValue((Attribute)fv2.elementAt(1), Double.parseDouble(vals[2]));
                this.muertes.add(m);
                
                //Se lee la siguiente linea
                linea = lec.readLine();
            }
            //Se crea el gráfico con el set de contagios y de muertes
           this.crearGrafico(contagios, muertes);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, 
                "No se encontro el archivo covid19datos.csv",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, 
                "Error al leer el archivo covid19datos.csv",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, 
                "Error en la conversion de numeros",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void verGrafico(){
        
    }

    private void crearGrafico(Instances c, Instances m){
        //Se crea el dataset donde se añaden los datos a mostrar en el gráfico
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        //Variables auxiliares para leer los datos
        Instance aux;
        double[] valores;
        //Se recorre el primer objeto Instances y se añaden sus datos al dataset
        for (int i=0; i<c.numInstances(); i++){
            aux=c.instance(i);
            if(aux.equals(null)) break;
            valores=aux.toDoubleArray();
            dataset.addValue(valores[1], "Contagios", String.valueOf(valores[0]));
        }
        //Se recorre el segundo objeto Instances y se añaden sus datos al dataset
        for (int i=0; i<m.numInstances(); i++){
            aux=m.instance(i);
            if(aux.equals(null)) break;
            valores=aux.toDoubleArray();
            dataset.addValue(valores[1], "Muertes", String.valueOf(valores[0]));
        }
        //Se crea el gráfico con el dataset creado anteriormente
        grafico = ChartFactory.createLineChart("Estadisticas", 
                "Dias", "Personas", dataset, 
                PlotOrientation.VERTICAL, true, true, false);
    }
    
    /*private void insertarFuncion(double[] vals, String op){
        //Insertar la regresión lineal con los valores dados
        XYSeries regresion = new XYSeries("Regresion Lineal "+op);
        regresion.add(1, vals[0]+vals[2]);
        int dia = this.contagios.numInstances();
        regresion.add(dia, vals[0]*dia+vals[2]);
        this.grafico.
    }*/
    
    private double[] regresion_lineal(Instances elementos){
        //Regresión lineal del objeto Instances como parámetro
        try {
            //Se establece el atributo de la posición 1 como índice de clase
            elementos.setClassIndex(1);
            //Se construye un modelo y se hace la regresión
            LinearRegression modelo = new LinearRegression();
            modelo.buildClassifier(elementos);
            //El modelo se envía como un vector de doubles
            double[] valores = modelo.coefficients();
            return valores;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al hacer la regresion lineal",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    private double[] centroides(Instances elementos, int numCentros){
        //Centroides del objeto Instances como parámetro
        try {
            //Se crea un modelo, se establecen 3 centroides, y se crean
            SimpleKMeans centros = new SimpleKMeans();
            centros.setNumClusters(numCentros);
            centros.buildClusterer(elementos);
            //Se obtienen los centros creados
            Instances datos = centros.getClusterCentroids();
            //Se expresan los centros como un vector de doubles y se envían
            Instance c;
            double[] vals = new double[datos.numInstances()*2];
            for (int i = 0; i<datos.numInstances(); i++){
                c = datos.instance(i);
                vals[2*i]=Double.parseDouble(c.toString(0));
                vals[2*i+1]=Double.parseDouble(c.toString(1));
            }
            return vals;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al determinar los centroides"+ex.toString(),
                "Centroides", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblOpciones = new javax.swing.JLabel();
        cmbOpciones = new javax.swing.JComboBox<>();
        lblEcuaciones = new javax.swing.JLabel();
        jScroll = new javax.swing.JScrollPane();
        txtaEcuaciones = new javax.swing.JTextArea();
        lblConsulta = new javax.swing.JLabel();
        txtConsulta = new javax.swing.JTextField();
        btnConsulta = new javax.swing.JButton();
        btnEcuaciones = new javax.swing.JButton();
        btnCentros = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtaCentros = new javax.swing.JTextArea();
        lblResp = new javax.swing.JLabel();
        btnGrafico = new javax.swing.JButton();
        lblCentros = new javax.swing.JLabel();
        txtCentros = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitulo.setFont(new java.awt.Font("Cambria", 0, 14)); // NOI18N
        lblTitulo.setText("Analisis de Casos por COVID19 - Perú");

        lblOpciones.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        lblOpciones.setText("Opciones:");

        cmbOpciones.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        cmbOpciones.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Contagios", "Muertes" }));

        lblEcuaciones.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        lblEcuaciones.setText("Ecuaciones:");

        txtaEcuaciones.setEditable(false);
        txtaEcuaciones.setColumns(20);
        txtaEcuaciones.setRows(5);
        jScroll.setViewportView(txtaEcuaciones);

        lblConsulta.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        lblConsulta.setText("Ingrese un día para obtener una predicción:");

        txtConsulta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtConsultaKeyTyped(evt);
            }
        });

        btnConsulta.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        btnConsulta.setText("Buscar");
        btnConsulta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnConsultaMouseClicked(evt);
            }
        });

        btnEcuaciones.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        btnEcuaciones.setText("Ver Ecuaciones");
        btnEcuaciones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEcuacionesMouseClicked(evt);
            }
        });

        btnCentros.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        btnCentros.setText("Ver Centroides");
        btnCentros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCentrosMouseClicked(evt);
            }
        });

        txtaCentros.setEditable(false);
        txtaCentros.setColumns(20);
        txtaCentros.setRows(5);
        jScrollPane1.setViewportView(txtaCentros);

        lblResp.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N

        btnGrafico.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        btnGrafico.setText("Ver Gráfico");
        btnGrafico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnGraficoMouseClicked(evt);
            }
        });

        lblCentros.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        lblCentros.setText("Centroides:");

        txtCentros.setText("3");
        txtCentros.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCentrosKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFondoLayout.createSequentialGroup()
                        .addComponent(jScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(80, 80, 80)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(pnlFondoLayout.createSequentialGroup()
                        .addGap(156, 156, 156)
                        .addComponent(lblTitulo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFondoLayout.createSequentialGroup()
                        .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlFondoLayout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addComponent(btnEcuaciones)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCentros))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlFondoLayout.createSequentialGroup()
                                .addGap(159, 159, 159)
                                .addComponent(lblOpciones)
                                .addGap(62, 62, 62)
                                .addComponent(cmbOpciones, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlFondoLayout.createSequentialGroup()
                                .addComponent(lblEcuaciones)
                                .addGap(230, 230, 230)
                                .addComponent(lblCentros)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtCentros, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(86, 86, 86))))
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnConsulta)
                    .addComponent(lblConsulta))
                .addGap(44, 44, 44)
                .addComponent(txtConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFondoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblResp, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(218, 218, 218)
                .addComponent(btnGrafico)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo)
                .addGap(18, 18, 18)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOpciones)
                    .addComponent(cmbOpciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEcuaciones)
                    .addComponent(btnCentros))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEcuaciones)
                    .addComponent(lblCentros)
                    .addComponent(txtCentros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScroll)
                    .addComponent(jScrollPane1))
                .addGap(21, 21, 21)
                .addComponent(btnGrafico)
                .addGap(2, 2, 2)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtConsulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConsulta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnConsulta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblResp, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlFondo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pnlFondo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultaMouseClicked
        // TODO add your handling code here: Metodo para consultar los datos de un día
        //Se debe validar que se haya ingresado un día como parámetro
        if (this.txtConsulta.getText().equals("")){
            return;
        }
        //Se convierte el texto ingresado a número
        int dia = Integer.parseInt(this.txtConsulta.getText());
        //Se obtiene la ecuación que determina los contagios por día y se calcula
        //el valor de los contagios para el día
        double[] ec = this.regresion_lineal(contagios);
        if(ec.equals(null)) JOptionPane.showMessageDialog(this, 
                "No se pudo hacer la regresion lineal de la ecuacion Contagios",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        double valorC = Math.round(ec[0]*dia+ec[2]);
        //Se obtiene la ecuación que determina las muertes por día y se calcula
        //el valor de los contagios para el día
        ec = this.regresion_lineal(muertes);
        if(ec.equals(null)) JOptionPane.showMessageDialog(this, 
                "No se pudo hacer la regresion lineal de la ecuacion",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        double valorM = Math.round(ec[0]*dia+ec[2]);
        //Se muestran los valores obtenidos
        this.lblResp.setText("Para el dia "+dia+" se esperan "+
                valorC+" contagios y "+valorM+" muertes");
    }//GEN-LAST:event_btnConsultaMouseClicked

    private void btnEcuacionesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEcuacionesMouseClicked
        // TODO add your handling code here:Método para consultar las ecuaciones 
        //Variable donde se guardará la ecuación
        double[] ec;
        //Se determina si será la ecuación de contagios o de muertes
        if(this.cmbOpciones.getSelectedIndex()==0) ec = this.regresion_lineal(contagios);
        else ec = this.regresion_lineal(muertes);
        //Si no se pudo obtener la ecuación
        if(ec.equals(null)) JOptionPane.showMessageDialog(this, 
                "No se pudo hacer la regresion lineal de la ecuacion",
                "Regresion Lineal", JOptionPane.ERROR_MESSAGE);
        //Se muestra la ecuación obtenida
        StringBuilder sb = new StringBuilder();
        sb.append((this.cmbOpciones.getSelectedIndex()==0)?"N° Contagios = ":"N° Muertes = ");
        sb.append(ec[0]).append("*Dia + ").append(ec[2])
                .append("\nPendiente: ").append(ec[0])
                .append("\nPunto de corte: ").append(ec[2]);
        this.txtaEcuaciones.setText(sb.toString());
    }//GEN-LAST:event_btnEcuacionesMouseClicked

    private void btnCentrosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCentrosMouseClicked
        // TODO add your handling code here:Método para consultar los centroides
        //Variable donde se guardarán los centroides
        double[] cents;
        int numCentros = Integer.parseInt(this.txtCentros.getText());
        //Se determina si será los centroides de la ecuación de contagios o de muertes
        if(this.cmbOpciones.getSelectedIndex()==0) cents = this.centroides(contagios, numCentros);
        else cents = this.centroides(muertes, numCentros);
        //Si no se pudo obtener los centroides
        if(cents.equals(null)) JOptionPane.showMessageDialog(this, 
                "No se pudo obtener los centroides de la ecuacion",
                "Centroides", JOptionPane.ERROR_MESSAGE);
        //Se muestran los centroides obtenidos
        StringBuilder sb = new StringBuilder("Centros de la ecuacion:");
        for (int i=0; i<(cents.length)/2; i++){
            sb.append("\nDia: ").append(cents[2*i]).append("; Cantidad: ").append(cents[2*i]+1);
        }
        this.txtaCentros.setText(sb.toString());
    }//GEN-LAST:event_btnCentrosMouseClicked

    private void btnGraficoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGraficoMouseClicked
        // TODO add your handling code here: //Visualizar el gráfico creado
        //Se inserta el gráfico en un ChartPanel y se muestra con un JFrame
        ChartPanel panel = new ChartPanel(grafico);
        panel.setPreferredSize(new Dimension(560,367));
        JFrame frame = new JFrame("Informacion COVID19 Peru");
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }//GEN-LAST:event_btnGraficoMouseClicked

    private void txtConsultaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtConsultaKeyTyped
        // TODO add your handling code here: El cuadro de texto sólo debe aceptar dígitos
        char car = evt.getKeyChar();
        if (!Character.isDigit(car)) evt.consume();
    }//GEN-LAST:event_txtConsultaKeyTyped

    private void txtCentrosKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCentrosKeyTyped
        // TODO add your handling code here:El cuadro de texto sólo debe aceptar dígitos
        char car = evt.getKeyChar();
        if (!Character.isDigit(car)) evt.consume();
    }//GEN-LAST:event_txtCentrosKeyTyped

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Vista.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Vista().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCentros;
    private javax.swing.JButton btnConsulta;
    private javax.swing.JButton btnEcuaciones;
    private javax.swing.JButton btnGrafico;
    private javax.swing.JComboBox<String> cmbOpciones;
    private javax.swing.JScrollPane jScroll;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCentros;
    private javax.swing.JLabel lblConsulta;
    private javax.swing.JLabel lblEcuaciones;
    private javax.swing.JLabel lblOpciones;
    private javax.swing.JLabel lblResp;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JTextField txtCentros;
    private javax.swing.JTextField txtConsulta;
    private javax.swing.JTextArea txtaCentros;
    private javax.swing.JTextArea txtaEcuaciones;
    // End of variables declaration//GEN-END:variables
}

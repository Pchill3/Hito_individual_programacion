package hito_individual;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class SoloCrossfitPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Precios de los diferentes planes de trabajo
    private static final double PRICE_BEGINNER = 25.00;
    private static final double PRICE_INTERMEDIATE = 30.00;
    private static final double PRICE_ELITE = 35.00;
    
    // Precio de una sesión privada adicional y el precio de participar en una competición
    private static final double PRICE_PRIVATE_SESSION = 9.50;
    private static final double PRICE_COMPETITION_ENTRY = 22.00;

    // Número máximo de horas extra de sesiones privadas permitidas al mes
    private static final int MAX_PRIVATE_SESSION_HOURS = 5;
    
    // Campos de texto y combobox para la entrada de datos en la interfaz
    private JTextField nombreUsuarioField;
    private JComboBox<String> planTrabajoComboBox;
    private JTextField pesoActualField;
    private JTextField horasExtraMesField;
    private JTextField competicionesField;

    public SoloCrossfitPanel() {
        setLayout(null);
        initComponents();
    }


    private void initComponents() {
        // Crear etiqueta y campo de texto para el nombre del usuario
        JLabel nombreUsuarioLabel = new JLabel("Nombre del Usuario:");
        nombreUsuarioLabel.setBounds(10, 10, 150, 25);
        add(nombreUsuarioLabel);

        nombreUsuarioField = new JTextField();
        nombreUsuarioField.setBounds(170, 10, 200, 25);
        add(nombreUsuarioField);

        // Crear etiqueta y ComboBox para el plan de trabajo
        JLabel planTrabajoLabel = new JLabel("Plan de Trabajo:");
        planTrabajoLabel.setBounds(10, 40, 150, 25);
        add(planTrabajoLabel);

        planTrabajoComboBox = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Elite"});
        planTrabajoComboBox.setBounds(170, 40, 200, 25);
        add(planTrabajoComboBox);

        // Crear etiqueta y campo de texto para el peso actual
        JLabel pesoActualLabel = new JLabel("Peso Actual (kg):");
        pesoActualLabel.setBounds(10, 70, 150, 25);
        add(pesoActualLabel);

        pesoActualField = new JTextField();
        pesoActualField.setBounds(170, 70, 200, 25);
        add(pesoActualField);

        // Crear etiqueta y campo de texto para las horas extra al mes
        JLabel horasExtraMesLabel = new JLabel("Horas Extra al Mes (Máximo 5):");
        horasExtraMesLabel.setBounds(10, 100, 200, 25);
        add(horasExtraMesLabel);

        horasExtraMesField = new JTextField();
        horasExtraMesField.setBounds(220, 100, 150, 25);
        add(horasExtraMesField);

        // Crear etiqueta y campo de texto para las competiciones
        JLabel competicionesLabel = new JLabel("Competiciones (Máximo 2):");
        competicionesLabel.setBounds(10, 130, 200, 25);
        add(competicionesLabel);

        competicionesField = new JTextField();
        competicionesField.setBounds(220, 130, 150, 25);
        add(competicionesField);

        // Crear botones de calcular y mostrar usuarios
        JButton calcularButton = new JButton("Calcular");
        calcularButton.setBounds(100, 170, 100, 25);
        calcularButton.addActionListener(e -> mostrarResultado());
        add(calcularButton);

        JButton mostrarUsuariosButton = new JButton("Mostrar Usuarios");
        mostrarUsuariosButton.setBounds(220, 170, 150, 25);
        mostrarUsuariosButton.addActionListener(e -> mostrarUsuarios());
        add(mostrarUsuariosButton);
    }

    private void mostrarResultado() {
        // Obtener los valores ingresados por el usuario
        String nombreUsuario = nombreUsuarioField.getText();
        String planTrabajo = (String) planTrabajoComboBox.getSelectedItem();
        String pesoActualText = pesoActualField.getText();
        String horasExtraMesText = horasExtraMesField.getText();
        String competicionesText = competicionesField.getText();

        // Validar que todos los campos estén llenos
        if (nombreUsuario.isEmpty() || planTrabajo.isEmpty() || pesoActualText.isEmpty() || horasExtraMesText.isEmpty()
                || competicionesText.isEmpty()) {
            mostrarError("Por favor, introduce datos en todos los campos.");
            return;
        }

        double pesoActual, totalCosteEntrenamientos, costeCompeticiones;
        int horasExtraMes, competiciones;

        try {
            // Convertir los valores a los tipos adecuados
            pesoActual = Double.parseDouble(pesoActualText);
            horasExtraMes = Integer.parseInt(horasExtraMesText);
            competiciones = Integer.parseInt(competicionesText);
        } catch (NumberFormatException ex) {
            mostrarError("Carácter no válido en uno de los campos numéricos.");
            return;
        }

        // Validar las restricciones de horas extra y competiciones
        if (horasExtraMes > MAX_PRIVATE_SESSION_HOURS) {
            mostrarError("No se pueden más de 5 horas extras al mes.");
            return;
        }

        if (competiciones > 2) {
            mostrarError("No se pueden más de 2 competiciones por mes.");
            return;
        }

        // Validar la restricción para clientes beginners
        if (planTrabajo.equalsIgnoreCase("beginner") && competiciones != 0) {
            mostrarError("No se pueden presentar a competiciones clientes beginners.");
            return;
        }

        // Calcular el coste total
        totalCosteEntrenamientos = calcularCosteEntrenamientos(planTrabajo, horasExtraMes);
        costeCompeticiones = calcularCosteCompeticiones(competiciones);

        // Obtener la categoría de peso
        String categoriaPeso = obtenerCategoriaPeso(pesoActual);
        double costeTotalMes = totalCosteEntrenamientos + costeCompeticiones;

        // Crear el mensaje de resultado
        String resultado = "<html><strong>Información del usuario:</strong><br>"
                + "Nombre del usuario: " + nombreUsuario + "<br><br>"
                + "Gastos detallados del mes:<br>"
                + "- Coste total de entrenamientos: " + formatCurrency(totalCosteEntrenamientos) + "<br>"
                + "- Coste de competiciones: " + formatCurrency(costeCompeticiones) + "<br>"
                + "- Coste total del mes: " + formatCurrency(costeTotalMes) + "<br><br>"
                + "Comparación de peso actual (" + pesoActual + " kg) con categoría de peso en competición: " + categoriaPeso + "</html>";

        // Mostrar la ventana de resultado
        mostrarVentana("Resultado", resultado, 500, 300);

        try {
            // Conectar a la base de datos 
            Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/hito_programacion", "root", "");
            
            //guardar los datos del usuario
            String consulta = "INSERT INTO usuarios (nombre, plan_trabajo, peso, horas_extra, competiciones) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = conexion.prepareStatement(consulta);

            statement.setString(1, nombreUsuario);
            statement.setString(2, planTrabajo);
            statement.setDouble(3, Double.parseDouble(pesoActualText));
            statement.setInt(4, Integer.parseInt(horasExtraMesText));
            statement.setInt(5, Integer.parseInt(competicionesText));

            statement.executeUpdate();

            statement.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("no conecta");
            e.printStackTrace();
        }
    }

    private void mostrarUsuarios() {
        try {
            // Conectar a la base de datos y obtener los datos de los usuarios
            Connection conexion2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/hito_programacion", "root", "");

            String query = "SELECT * FROM usuarios ORDER BY id ASC";
            Statement statement = conexion2.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Crear el modelo de tabla
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("ID");
            tableModel.addColumn("Nombre");
            tableModel.addColumn("Plan de Trabajo");
            tableModel.addColumn("Peso Actual");
            tableModel.addColumn("Horas Extra al Mes");
            tableModel.addColumn("Competiciones");

            // Llenar la tabla con los datos de los usuarios
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String planTrabajo = resultSet.getString("plan_trabajo");
                double pesoActual = resultSet.getDouble("peso");
                int horasExtraMes = resultSet.getInt("horas_extra");
                int competiciones = resultSet.getInt("competiciones");

                Vector<Object> row = new Vector<>();
                row.add(id);
                row.add(nombre);
                row.add(planTrabajo);
                row.add(pesoActual);
                row.add(horasExtraMes);
                row.add(competiciones);

                tableModel.addRow(row);
            }

            resultSet.close();
            statement.close();
            conexion2.close();

            // Crear la ventana de usuarios y mostrar la tabla
            JFrame ventanaUsuarios = new JFrame("Usuarios");
            ventanaUsuarios.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventanaUsuarios.setSize(600, 400);
            ventanaUsuarios.setLocationRelativeTo(null);

            JTable tablaUsuarios = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
            ventanaUsuarios.getContentPane().add(scrollPane, BorderLayout.CENTER);

            ventanaUsuarios.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al mostrar los usuarios.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static double calcularCosteEntrenamientos(String planTrabajo, int horasExtraMes) {
        double costeEntrenamientos = 0.0;

        // Calcular el costo de los entrenamientos según el plan de trabajo
        switch (planTrabajo.toLowerCase()) {
            case "beginner":
                costeEntrenamientos = PRICE_BEGINNER * 4;
                break;
            case "intermediate":
                costeEntrenamientos = PRICE_INTERMEDIATE * 4;
                break;
            case "elite":
                costeEntrenamientos = PRICE_ELITE * 4;
                break;
        }

        // Calcular el costo de las horas extra
        double costeHorasExtra = PRICE_PRIVATE_SESSION * Math.min(horasExtraMes, MAX_PRIVATE_SESSION_HOURS);
        costeEntrenamientos += costeHorasExtra;

        return costeEntrenamientos;
    }

    private static double calcularCosteCompeticiones(int competiciones) {
        return PRICE_COMPETITION_ENTRY * competiciones;
    }

    //creas la condicion de que dependiendo de que peso metas te devuelva cada frase
    private static String obtenerCategoriaPeso(double pesoActual) {
        String categoriaPeso;

        if (pesoActual < 56.7) {
            categoriaPeso = "Light Flyweight";
        } else if (pesoActual < 66) {
            categoriaPeso = "Flyweight";
        } else if (pesoActual <= 73.0) {
            categoriaPeso = "Lightweight";
        } else if (pesoActual <= 81.0) {
            categoriaPeso = "Light Middleweight";
        } else if (pesoActual <= 90.0) {
            categoriaPeso = "Middleweight";
        } else if (pesoActual <= 100.0) {
            categoriaPeso = "Light Heavyweight";
        } else {
            categoriaPeso = "Heavyweight";
        }

        return categoriaPeso;
    }

    private static String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return "$" + formatter.format(amount);
    }

    //metodo para crear la ventana con sus especificaciones
    private static void mostrarVentana(String titulo, String mensaje, int width, int height) {
        JFrame ventana = new JFrame(titulo);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(width, height);
        ventana.setLocationRelativeTo(null);

        JLabel label = new JLabel(mensaje);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        ventana.getContentPane().add(label);

        ventana.setVisible(true);
    }
}

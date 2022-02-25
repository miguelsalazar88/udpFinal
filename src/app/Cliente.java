package app;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Cliente extends JFrame implements ActionListener {

    //Elementos del Protocolo UDP
    private DatagramSocket datagramSocket; //Instancia de socket para el envío de datos.
    private InetAddress inetAddress; // Instancia de Dirección IP
    private byte[] buffer; // Buffer de envío de datos para el protocolo UDP

    //Elementos de la Ventana
    private JLabel labelCliente = new JLabel("Cliente:"); //Label del JTArea
    private JButton botonCargar = new JButton("Cargar");
    private JButton botonEnviar = new JButton("Enviar"); // Boton para enviar mensaje
    private JTextArea textoCliente = new JTextArea(); //JTArea para mostrar el contenido del mensaje
    private JScrollPane scrollPaneTexto = new JScrollPane(textoCliente); // Scroll Pane para el JTA

    //Constructor del Objeto CLiente
    public Cliente(DatagramSocket datagramSocket, InetAddress inetAddress) {

        //UDP
        this.datagramSocket = datagramSocket; //Se asigna el socket a la variable
        this.inetAddress = inetAddress; // Se asigna la dirección IP a la variable

        //Ventana
        this.setSize(600,700); //Tamaño de la ventana
        this.setVisible(true); // Visibilidad de la ventana
        this.setLayout(null); // Sin layout
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); //Se termina el programa cuando se cierra la ventana
        this.setTitle("Ventana Cliente"); // Título de la ventana
        this.initComponents(); //Método que inicia los componentes del JFrame
    }

    private void initComponents() {
        this.add(labelCliente); //Label del JTA
        this.labelCliente.setBounds(10,10,70,30); //Tamaño del label
        this.add(scrollPaneTexto); // Scroll pane para el JTA y poder ver todo el texto
        this.textoCliente.setEditable(false); // Se deja el JTA sin opción de editar
        this.scrollPaneTexto.setBounds(10,50,400,550); // Tamaño del JTA
        this.add(botonEnviar); // Boton para enviar el mensaje
        this.botonEnviar.setBounds(110,630,100,30); //Tamaño del boton enviar
        this.botonEnviar.addActionListener(this); //Se agrega un Actionlistener al boton enviar
        this.add(botonCargar);
        this.botonCargar.setBounds(10,630,100,30);
        this.botonCargar.addActionListener(this);
    }

    public void cargarTexto(String path) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while ((line = br.readLine()) != null){
                this.textoCliente.setText(this.textoCliente.getText() + "\n" + line);
            }
        }
    }

    public void enviarMensaje() throws BadLocationException, IOException, InterruptedException {

        FileWriter myWriter = new FileWriter("src/files/textoResultado.txt");

        for (int linea = 0; linea < textoCliente.getLineCount(); linea++) {
            int start = textoCliente.getLineStartOffset(linea);
            int end = textoCliente.getLineEndOffset(linea);
            String mensaje = textoCliente.getText(start, end-start);
            buffer = mensaje.getBytes(); //Se convierte el mensaje a Bytes

            //Se crea una instancia de DatagramPacket para poder encapsular el mensaje, y enviarlo por el puerto
            // 1234
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, inetAddress, 1234);

            //Se envía el mensaje a través del Socket
            datagramSocket.send(datagramPacket);

            // Se espera la confirmación de la recepción del mensaje por parte del servidor
            datagramSocket.receive(datagramPacket);

            //Recibe el DatagramPacket que contiene la confirmación de recepción del mensaje por parte
            //del servidor
            String mensajeDelServidor = new String(datagramPacket.getData(),0,datagramPacket.getLength());
            Thread.sleep(100);


            myWriter.write(mensaje);


        }

            myWriter.close();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(this.botonCargar)){
            try {
                cargarTexto("src/files/textoEjemplo.txt");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }



        if(e.getSource().equals(this.botonEnviar)){
            try {
                enviarMensaje();
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        Cliente cliente = new Cliente(datagramSocket, inetAddress);
    }
}

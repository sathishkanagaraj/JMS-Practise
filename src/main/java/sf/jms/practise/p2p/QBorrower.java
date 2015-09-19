package sf.jms.practise.p2p;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: sathih
 * Date: 18/9/14
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class QBorrower {

    private QueueConnection queueConnection = null;
    private QueueSession queueSession = null;
    private Queue responseQueue = null;
    private Queue requestQueue = null;

    public QBorrower(String queueConFactory, String responseQueue, String requestQueue) {
        try {
            Context initialContext = new InitialContext();
            QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) initialContext.lookup(queueConFactory);
            queueConnection = queueConnectionFactory.createQueueConnection();
            //once the queue conncetion is established then create a queueSession

            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            // after session lookup for queue because it is one of an administered objects
           this.requestQueue = (Queue)initialContext.lookup(requestQueue);
           this.responseQueue = (Queue)initialContext.lookup(responseQueue);

            //set up is complete , start the connection
            queueConnection.start();
        } catch (NamingException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void sendMessage(double salary, double loanAmount){
        try {
            // create JMS message
            MapMessage mapMessage = queueSession.createMapMessage();
            mapMessage.setDouble("SALARY",salary);
            mapMessage.setDouble("LOAN_AMOUNT",loanAmount);
            mapMessage.setJMSReplyTo(responseQueue);
            // create sender and send the message
            QueueSender queueSender = queueSession.createSender(requestQueue);
            queueSender.send(mapMessage);
            // wait and see if you loan request is accepted or rejected
            String filter = "JMSCorrelationId = '"+ mapMessage.getJMSMessageID()+"'";
            QueueReceiver queueReceiver = queueSession.createReceiver(responseQueue, filter);
            TextMessage textMessage = (TextMessage) queueReceiver.receive(30000);
            if(textMessage == null){
                System.out.println(" QLender is not responding");
            }else {
                String text = textMessage.getText();
                System.out.println("text = " + text);
            }
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void exit(){
        try {
            queueConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        QBorrower qBorrower = new QBorrower(args[0], args[1], args[2]);
        // read all standard input and send it as a message
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("QBorrower application has started");
        System.out.println("press enter to quit the application");
        System.out.println("Enter salary and loan_amount e.g 20000,50000");

        while(true){
            System.out.println("> ");
            try {
                String loanRequestInput = bufferedReader.readLine();
                if(loanRequestInput == null || loanRequestInput.trim().length() <=0){
                              qBorrower.exit();
                }
                // parse the input i.e separate the input string and assign salary and loan amount
                StringTokenizer tokenizer = new StringTokenizer(loanRequestInput,",");
                double salary = Double.valueOf(tokenizer.nextToken().trim()).doubleValue();
                double loanAmount = Double.valueOf(tokenizer.nextToken().trim()).doubleValue();
                qBorrower.sendMessage(salary, loanAmount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package sf.jms.practise.p2p;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: sathih
 * Date: 19/9/14
 * Time: 10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class QLender implements MessageListener {
    QueueConnection queueConnection = null;
    QueueSession queueSession = null;
    Queue queue = null;
    public QLender(String queuecf, String requestQueue) {
        try {
            Context context = new InitialContext();
            QueueConnectionFactory connectionFactory = (QueueConnectionFactory) context.lookup(queuecf);
            queueConnection = connectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = (Queue) context.lookup(requestQueue);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            queueReceiver.setMessageListener(this);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(Message message) {
        // get data from the message

        boolean accepted = false;
        MapMessage mapMessage = (MapMessage) message;
        try {
            double salary = mapMessage.getDouble("SALARY");
            double loan_amount = mapMessage.getDouble("LOAN_AMOUNT");
            if(loan_amount<200000){
                accepted = true;
            }
            System.out.println("loan_amount = " + loan_amount);
            System.out.println("salary = " + salary);
            // result obtained , now send it back to the borrower
            // it is not necessary that the incoming message should be same as outgoing
            TextMessage textMessage = queueSession.createTextMessage();
            textMessage.setText(accepted ? "Accepted!!!" : "Rejected");
            textMessage.setJMSCorrelationID(message.getJMSMessageID());
            // create sender and send the message
            QueueSender queueSender = queueSession.createSender((Queue) textMessage.getJMSReplyTo());
            queueSender.send(textMessage);

            System.out.println("Waiting for loan request!!!");

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
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        String qcf = null;
        String rq = null;
        if(args.length == 2){
            qcf = args[0];
            rq = args[1];
        }else{
            System.out.println("Invalid input parameters");
            System.exit(0);
        }
        QLender qLender = new QLender(qcf, rq);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("QLender application started.....");
            System.out.println("press enter to quit application.");
            bufferedReader.readLine();
            qLender.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

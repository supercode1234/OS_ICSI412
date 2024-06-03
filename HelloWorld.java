public class HelloWorld extends UserlandProcess{


    @Override
    public void main() {
        while(true){
            System.out.println("Hello world");
            cooperate();

            try {
                Thread.sleep(50);
//                OS.sleep
            } catch (Exception e){
                Thread.currentThread().interrupt();
            }

        }
    }
}

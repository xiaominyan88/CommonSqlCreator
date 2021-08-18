package predictions;


public class Predictions {
    private Predictions(){
        //Empty
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs){
        if(expression){
            throw new IllegalArgumentException(
                    format(errorMessageTemplate,errorMessageArgs)
            );
        }
    }

    public static String format(String template,Object... objects){
        template = String.valueOf(template);
        StringBuilder builder = new StringBuilder(template.length() + 16*objects.length);
        int templateStart = 0;
        int i = 0;
        while(i<objects.length){
            int placeHolderStart = template.indexOf("%s",templateStart);
            if(placeHolderStart == -1){
                break;
            }
            builder.append(template.substring(templateStart,placeHolderStart));
            builder.append(objects[i++]);
            templateStart = placeHolderStart + 2;
        }
        builder.append(template.substring(templateStart));

        if (i < objects.length) {
            builder.append(" [");
            builder.append(objects[i++]);
            while (i < objects.length) {
                builder.append(", ");
                builder.append(objects[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }

    public static <T> boolean checkNotNull(T reference){

        if(reference == null) {
            return false;
        }else{
            return true;
        }
    }
}

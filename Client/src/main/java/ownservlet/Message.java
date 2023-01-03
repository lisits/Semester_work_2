package ownservlet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private byte type;
    private byte[] data;

    public Message(byte type){
        this.type = type;
        data = new byte[0];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i = i + 2){
            char a = (char) (data[i]<<8 | data[i+1]);
            sb.append(a);
        }
        return sb.toString();
    }
}


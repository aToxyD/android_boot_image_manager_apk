package dz.atoxyd.ABM.util;

//original author: atoxyd 
//modified by: ........

public class Item implements Comparable<Item>{
    private String name;
    private String data;
    private String date;
    private String path;
    private String image;

    public Item(String n,String d, String dt, String p, String img){
        name = n;
        data = d;
        date = dt;
        path = p;
        image = img;

    }
    public String getName(){
        return name;
    }
    public void setName(String d){
        this.name=d;
    }
    public String getData(){
        return data;
    }
    public void setData(String d){
        this.data=d;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String d){
        this.date=d;
    }
    public String getPath(){
        return path;
    }
    public String getImage() {
        return image;
    }
    public int compareTo(Item o) {
        if(this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}

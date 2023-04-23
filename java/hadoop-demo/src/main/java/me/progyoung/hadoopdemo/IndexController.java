package me.progyoung.hadoopdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;


import javax.annotation.PostConstruct;


@RestController
@RequestMapping("/api")
public class IndexController {
    private Configuration hadoopConfig;

    // 初始化Hadoop客户端配置和文件系统
    @PostConstruct
    public void init() throws IOException {
        hadoopConfig = new Configuration();
        hadoopConfig.set("fs.defaultFS", "namenode:9000"); // 设置Hadoop NameNode的地址
    }

    // 处理HTTP GET请求，读取指定路径的文件，并将内容作为响应返回
    @GetMapping("/files/{filename}")
    public ResponseEntity<String> readHadoopFile(@PathVariable("filename") String filename) throws IOException {
        Path hadoopPath = new Path("hdfs://namenode:9000/input/" + filename);

        String contentStr = "";
        try (FileSystem fs = hadoopPath.getFileSystem(hadoopConfig);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(hadoopPath)))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            contentStr = content.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filename, e);
        }


        return new ResponseEntity<>(contentStr, HttpStatus.OK);
    }

    @GetMapping("/movies")
    public ResponseEntity<String> randomMovies() throws IOException {

        List<Movie> movies = new ArrayList<>();

        movies.add(new Movie("The Shawshank Redemption", this.rand()));
        movies.add(new Movie("The Godfather", this.rand()));
        movies.add(new Movie("The Godfather: Part II", this.rand()));
        movies.add(new Movie("The Dark Knight", this.rand()));
        movies.add(new Movie("12 Angry Men", this.rand()));
        movies.add(new Movie("Schindler's List", this.rand()));
        movies.add(new Movie("Pulp Fiction", this.rand()));
        movies.add(new Movie("The Lord of the Rings: The Return of the King", this.rand()));
        movies.add(new Movie("The Good, the Bad and the Ugly", this.rand()));
        movies.add(new Movie("Fight Club", this.rand()));
        movies.add(new Movie("Star Wars: Episode V - The Empire Strikes Back", this.rand()));

        // movies 转化为json字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(movies);

        return new ResponseEntity<>(jsonString, HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    public static class Movie {
        String name;
        Integer number;
    }

    // 随机生成100-500的整数
    public Integer rand() {
        return (int) (Math.random() * 500 + 100);
    }
}

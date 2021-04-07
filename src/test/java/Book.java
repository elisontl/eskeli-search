import com.eskeli.search.annotation.KeliSearchIdxEntity;
import lombok.AllArgsConstructor;

@KeliSearchIdxEntity(indexCover = true, wholeAreaIndex = true)
@AllArgsConstructor
public class Book {

    private long id;

    private String bookName;

    private String bookContent;

}

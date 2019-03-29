DELIMITER //

create function add_authorAAN ( name_ varchar(255) )
  RETURNS BIGINT(20) DETERMINISTIC MODIFIES SQL DATA
  BEGIN
    declare aid int UNSIGNED default 0;

    if name_ is NULL then
      return aid;
    end if;

    select a.id into aid from authors_aan as a WHERE a.name = name_;
    if aid = 0 then
      -- create new author
      insert into authors_aan set name = name_;
      select LAST_INSERT_ID() into aid;
    end if;

    return aid;
  END //

create function add_documentAAN ( file_ varchar(255), title_ varchar(768), venue_ varchar(255), year_ INT)
  RETURNS BIGINT(20) DETERMINISTIC MODIFIES SQL DATA
  BEGIN
    declare did int UNSIGNED default 0;

    if file_ is NULL  THEN
      return did;
    END IF;

    if title_ is NULL then
      return did;
    end if;

    select d.id into did from documents_aan as d WHERE d.file = file_;
    if did = 0 then
      -- create new document
      insert into documents_aan set file = file_, title = title_, venue = venue_, year = year_;
      select LAST_INSERT_ID() into did;
    end if;

    return did;
  END //

create function add_collaborationAAN2 ( name1 varchar(255), name2 varchar(255) )
  RETURNS BOOLEAN DETERMINISTIC MODIFIES SQL DATA
  BEGIN
    declare c int UNSIGNED default 0;

    if name1 is NULL then
      return 0;
    end if;

    if name2 is NULL then
      return 0;
    end if;

    select a.count into c from collaborations_aan2 as a WHERE a.author1 = name1 AND a.author2 = name2;
    if c = 0 then

      select a.count into c from collaborations_aan2 as a WHERE a.author1 = name2 AND a.author2 = name1;
      if c = 0 then
        -- create new collaboration
        insert into collaborations_aan2 set author1 = name1, author2 = name2, count = 1;
        RETURN 1;
      ELSEIF c != 0 THEN
        UPDATE collaborations_aan2 SET count = count + 1 WHERE author1 = name2 AND author2 = name1;
        RETURN 1;
      END IF;
    ELSEIF c != 0 THEN
      UPDATE collaborations_aan2 SET count = count + 1 WHERE author1 = name1 AND author2 = name2;
      RETURN 1;
    end if;

    return 0;
  END //

create function add_collaborationAAN ( name1 varchar(255), name2 varchar(255) )
  RETURNS BOOLEAN DETERMINISTIC MODIFIES SQL DATA
  BEGIN
    declare aid varchar(255) default null;

    if name1 is NULL then
      return 0;
    end if;

    if name2 is NULL then
      return 0;
    end if;

    select a.author1 into aid from collaborations_aan as a WHERE a.author1 = name1 AND a.author2 = name2;
    if aid IS NULL then

      select a.author1 into aid from collaborations_aan as a WHERE a.author1 = name2 AND a.author2 = name1;
      if aid IS NULL then
        -- create new collaboration
        insert into collaborations_aan set author1 = name1, author2 = name2;
        RETURN 1;
      END IF;
    end if;

    return 0;
  END //

create function add_publicationAAN ( author_ VARCHAR(255), document_ varchar(255) )
  RETURNS BOOLEAN DETERMINISTIC MODIFIES SQL DATA
  BEGIN
    declare name VARCHAR(255) default NULL;

    if author_ IS NULL then
      return 0;
    ELSEIF document_ IS NULL THEN
      RETURN 0;
    end if;

    select p.author into name from publications_aan as p WHERE p.author = author_ AND  p.document = document_;
    if name is NULL THEN
      -- create new relation
      INSERT into publications_aan SET author = author_, document = document_;
      RETURN 1;
    END IF;

    return 0;
  END //

CREATE FUNCTION add_citationAAN(outgoing VARCHAR(255), incoming VARCHAR(255))
  RETURNS BOOLEAN DETERMINISTIC MODIFIES SQL DATA
  BEGIN
    declare outgoingfile_ varchar(255) default NULL;
    declare incomingfile_ varchar(255) default NULL;
    declare testfile_ varchar(255) default NULL;

    if outgoing IS NULL then
      return 0;
    ELSEIF incoming IS NULL THEN
      RETURN 0;
    end if;

    -- check if outgoing file exists
    select d.file into outgoingfile_ from documents_aan as d WHERE d.file = outgoing;
    if outgoingfile_ is NULL THEN
      return 0;
    END IF;

    -- check if incoming file exists
    select d.file into incomingfile_ from documents_aan as d WHERE d.file = incoming;
    if incomingfile_ is NULL THEN
      return 0;
    END IF;

    -- check if the citation already exists
    select c.outgoing_file into testfile_ from citations_aan as c WHERE c.outgoing_file = outgoing AND c.incoming_file = incoming;
    if testfile_ IS NULL THEN
      -- create new relation
      INSERT into citations_aan SET outgoing_file = outgoing, incoming_file = incoming;
      RETURN 1;
    END IF;

    return 0;
  END //

--
-- Tabellenstruktur für Tabelle `authors_aan`
--

DROP TABLE IF EXISTS `authors_aan`;
CREATE TABLE `authors_aan` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `alt_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `documents_aan`
--

DROP TABLE IF EXISTS `documents_aan`;
CREATE TABLE `documents_aan` (
  `id` bigint(20) NOT NULL,
  `file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(768) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `year` INT DEFAULT NULL,
  `venue` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `publications_aan`
--

DROP TABLE IF EXISTS `publications_aan`;
CREATE TABLE `publications_aan` (
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `document` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `collaborations_aan`
--

DROP TABLE IF EXISTS `collaborations_aan`;
CREATE TABLE `collaborations_aan` (
  `author1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `author2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `collaborations_aan2`
--

DROP TABLE IF EXISTS `collaborations_aan2`;
CREATE TABLE `collaborations_aan2` (
  `author1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `author2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `count` INT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `citations_aan`
--

DROP TABLE IF EXISTS `citations_aan`;
CREATE TABLE `citations_aan` (
  `outgoing_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `incoming_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;


-- --------------------------------------------------------

--
-- Indizes für die Tabelle `documents_aan`
--
ALTER TABLE `documents_aan`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_file` (`file`);

--
-- Indizes für die Tabelle `authors_aan`
--
ALTER TABLE `authors_aan`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_name` (`name`);

--
-- Indizes für die Tabelle `citations_aan`
--
ALTER TABLE `citations_aan`
  ADD PRIMARY KEY (`incoming_file`,`outgoing_file`);

--
-- Indizes für die Tabelle `publications_aan`
--
ALTER TABLE `publications_aan`
  ADD PRIMARY KEY (`author`,`document`);

--
-- Indizes für die Tabelle `collaborations_aan`
--
ALTER TABLE `collaborations_aan`
  ADD PRIMARY KEY (`author1`,`author2`);

--
-- Indizes für die Tabelle `collaborations_aan2`
--
ALTER TABLE `collaborations_aan2`
  ADD PRIMARY KEY (`author1`,`author2`);

--
-- AUTO_INCREMENT für Tabelle `authors_aan`
--
ALTER TABLE `authors_aan`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `documents_aan`
--
ALTER TABLE `documents_aan`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Constraints der Tabelle `citations_aan`
--
ALTER TABLE `citations_aan`
  ADD CONSTRAINT `outgoing` FOREIGN KEY (`outgoing_file`) REFERENCES `documents_aan` (`file`),
  ADD CONSTRAINT `incoming` FOREIGN KEY (`incoming_file`) REFERENCES `documents_aan` (`file`);

--
-- Constraints der Tabelle `publications_aan`
--
ALTER TABLE `publications_aan`
  ADD CONSTRAINT `author` FOREIGN KEY (`author`) REFERENCES `authors_aan` (`name`),
  ADD CONSTRAINT `document` FOREIGN KEY (`document`) REFERENCES `documents_aan` (`file`);

--
-- Constraints der Tabelle `collaborations_aan`
--
ALTER TABLE `collaborations_aan`
  ADD CONSTRAINT `author1` FOREIGN KEY (`author1`) REFERENCES `authors_aan` (`name`),
  ADD CONSTRAINT `author2` FOREIGN KEY (`author2`) REFERENCES `authors_aan` (`name`);

--
-- Constraints der Tabelle `collaborations_aan2`
--
ALTER TABLE `collaborations_aan2`
  ADD CONSTRAINT `c2author1` FOREIGN KEY (`author1`) REFERENCES `authors_aan` (`name`),
  ADD CONSTRAINT `c2author2` FOREIGN KEY (`author2`) REFERENCES `authors_aan` (`name`);

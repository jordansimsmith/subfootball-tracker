/*
 * This file is generated by jOOQ.
 */
package com.jordansimsmith.subfootball.tracker.jooq;


import com.jordansimsmith.subfootball.tracker.jooq.tables.Content;
import com.jordansimsmith.subfootball.tracker.jooq.tables.records.ContentRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ContentRecord> CONTENT_PKEY = Internal.createUniqueKey(Content.CONTENT, DSL.name("content_pkey"), new TableField[] { Content.CONTENT.ID }, true);
}
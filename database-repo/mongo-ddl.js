// =============================================================================
// mongo-ddl.js  —  CURRENT FULL COLLECTIONS (squash mode)
// Document-store counterpart of oracle-ddl.sql. Every collection is created
// with a $jsonSchema validator (the document-store equivalent of NOT NULL /
// type / range constraints). No dropCollection in the source of truth.
//
// Snapshot state: pending change PROJ-102 (add AUDIT_LOG) has NOT yet been
// synced; it is in-flight in sample-service/docs/db. Stage 3 is what folds it
// into this file and the changelog.
// =============================================================================

db.createCollection('ACCOUNT_SETTING', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      title: 'ACCOUNT_SETTING Collection Validation',
      required: ['_id', 'USERNAME', 'LOCALE', 'UPDATED_AT'],
      properties: {
        _id: {
          bsonType: 'objectId',
          description: 'Primary key.'
        },
        USERNAME: {
          bsonType: 'string',
          description: 'Owning login name; required.'
        },
        LOCALE: {
          bsonType: 'string',
          enum: ['en', 'zh-TW', 'ja'],
          description: 'UI locale; one of the allowed values; required.'
        },
        NOTIFICATIONS_ENABLED: {
          bsonType: 'bool',
          description: 'Whether notifications are enabled, if present.'
        },
        UPDATED_AT: {
          bsonType: 'date',
          description: 'Last update timestamp; required.'
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});

db.ACCOUNT_SETTING.createIndex(
  { USERNAME: 1 },
  { unique: true, name: 'ACCOUNT_SETTING_UK' }
);

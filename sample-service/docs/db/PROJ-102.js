// TAG: PROJ-102
// SCHEMA: ACCOUNT
// TYPE: DDL
// COLLECTIONS: AUDIT_LOG
// DESCRIPTION: Create AUDIT_LOG collection for operation audit trail
// BREAKING: N

db.createCollection('AUDIT_LOG', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      title: 'AUDIT_LOG Collection Validation',
      required: ['_id', 'USERNAME', 'ACTION', 'TARGET_TYPE', 'OCCURRED_AT'],
      properties: {
        _id: {
          bsonType: 'objectId',
          description: 'Primary key.'
        },
        USERNAME: {
          bsonType: 'string',
          description: 'Login name of the user who performed the action; required.'
        },
        ACTION: {
          bsonType: 'string',
          enum: ['CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT'],
          description: 'Type of action performed; one of the allowed values; required.'
        },
        TARGET_TYPE: {
          bsonType: 'string',
          description: 'Entity type that was acted upon (e.g. ACCOUNT, USER); required.'
        },
        TARGET_ID: {
          bsonType: 'string',
          description: 'Identifier of the target entity, if applicable.'
        },
        OCCURRED_AT: {
          bsonType: 'date',
          description: 'Timestamp when the action occurred; required.'
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: 'strict',
  validationAction: 'error'
});

db.AUDIT_LOG.createIndex(
  { USERNAME: 1, OCCURRED_AT: -1 },
  { name: 'AUDIT_LOG_USERNAME_OCCURRED_AT' }
);

db.AUDIT_LOG.createIndex(
  { OCCURRED_AT: -1 },
  { name: 'AUDIT_LOG_OCCURRED_AT' }
);

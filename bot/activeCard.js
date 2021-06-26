const MethodNames = require('./actionMethodNames.json')
function createMessage(req, buildIndex) {
  let body = req.body;
  let message = {
    "actionResponse": { "type": "NEW_MESSAGE" },
    previewText: body.header.subtitle + " " + body.header.title,
    "cards": [{
      "header": { ...body.header },
      "sections": []
    }]
  };
  if (body.sections) {
    message.cards[0].sections = message.cards[0].sections.concat(body.sections)
  }
  message.cards[0].sections.push({
    "header": "Cause",
    "widgets": [{
      "textParagraph": { "text": `${body.cause}` },
      "buttons": [{
        "textButton":
        {
          "text": "Console",
          "onClick": {
            "openLink": {
              "url": `https://www.google.com/url?q=${body.console}`
            }
          }
        }
      }]
    }]
  })

  message.cards[0].sections.push({
    "header": "Inventory",
    "widgets": [
      {
        "textParagraph": { "text": ` <a href="https://www.google.com/url?q=${req.protocol + '://' + req.get('host')}/inventory/${body.inventory}">Visit Inventory</a> ` }
      }
    ]
  })

  message.cards[1] = { sections: [] }

  message.cards[1].sections.push({
    "header": "Waiting for approve",
    widgets: [{
      "buttons": [{
        "textButton": {
          "text": "Ok",
          "onClick": {
            "action": {
              "actionMethodName": MethodNames.approve_build,
              "parameters": [
                {
                  "key": "buildIndex",
                  "value": `${buildIndex}`
                }
              ]
            }
          }
        }
      }, {
        "textButton": {
          "text": "Cancel",
          "onClick": {
            "action": {
              "actionMethodName": MethodNames.cancel_build,
              "parameters": [
                {
                  "key": "buildIndex",
                  "value": `${buildIndex}`
                }
              ]
            }
          }
        }
      },
      ]
    }
    ]
  })
  return message
}


module.exports = {
  createMessage
}
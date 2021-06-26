
  const express = require('express');
  const googleChat = require('./googleChat')
  const Redis = require("ioredis");
  const redis = new Redis();
  const createCard = require('./activeCard');
  const helper = require('./helper')
  const fs = require("fs");
  require('dotenv').config()
  const MethodNames = require('./actionMethodNames.json');
  const PORT = process.env.PORT || 9000;

  
  const app = express()
      .use(express.urlencoded({extended: false}))
      .use(express.json());
  
  app.post('/bot', async (req, res) => {
    let message;
    let body = {}

    if (req.body.type == "CARD_CLICKED") {
       let buildIndex = req.body.action.parameters[0].value 
       let build = await redis.get(buildIndex)
       build = JSON.parse(build)

        let chatData = {
          message: buildIndex,
          user: req.body.user.displayName
        }

        let cards = req.body.message.cards
        body.header  = cards.header

      if (req.body.action.actionMethodName == MethodNames.approve_build) {

        helper.fetchWebHook(build.url,{"status":200, ...chatData })
      
        cards[1].sections[0].widgets[0] = {
          textParagraph: { text: `Build approved by ${chatData.user}`}
        }
        cards[1].sections[0].header = `Action`
        message = {actionResponse: {"type": "UPDATE_MESSAGE"}, cards}
      }

      if (req.body.action.actionMethodName == MethodNames.cancel_build) {
          cards[1].sections[0].widgets[0] = {
          textParagraph: {text: `Build Canceled by ${chatData.user}`}
        }
        cards[1].sections[0].header = `Action`
        helper.fetchWebHook(build.url,{"status":400, ...chatData})
        message = {actionResponse: {"type": "UPDATE_MESSAGE"}, cards}
      }

      googleChat.updateMessages(build.messages,message)
    }
   
    res.send(message)
  });

  app.all('/jenkins', async (req, res)  => {
    if(req.body.message){
      let build = await redis.get(req.body.message)
      build = JSON.parse(build)
      googleChat.getMessage(build.messages[0]).then( message => {
        message.data.cards[0].header = req.body.cards[0].header
        googleChat.updateMessages(build.messages,message.data)
      })

    }else {
      let key = Buffer.from(req.body.buildTag).toString('base64')
      let data = {
        url: req.body.url
      };
      
      let requestBody = createCard.createMessage(req, key)
      messages = await googleChat.boroadCastMessage(requestBody)
      data.messages = messages
      redis.set(key,JSON.stringify(data), 'EX', 3600 * 24 * 7);
    }
    res.send('ok')

  })

  app.get('/inventory*',async (req,res) => {
    let path = `${process.env.INVENTORY_PATH}${req.params[0]}.ini`
    const content = fs.readFileSync(path);
    res.header('Content-Type', 'application/javascript');
    res.send(content + `path: ${path}`)
  })

  let builds = {};
  app.post('/ansible',async (req,res) => {
    let key = Buffer.from(req.body.buildTag).toString('base64')
    let host = req.body.host
    delete req.body.host

    while (builds[key]) {
      await helper.sleep(100)
    }
    builds[key] = true

    let build = await redis.get(key);
    build = JSON.parse(build);


    await googleChat.getMessage(build.messages[0]).then( message => {
        message.data.cards[0].sections[0].widgets.push({
          keyValue: {
            bottomLabel: '',
            content: helper.objToString(req.body),
            topLabel: host
          }
        })
        return googleChat.updateMessages(build.messages,message.data)
    })
    delete builds[key] 
    res.send('ok')
  })

  app.listen(PORT, () => {
    console.log(`Server is running in port - ${PORT}`);
  });
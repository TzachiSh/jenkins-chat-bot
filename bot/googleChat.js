const {google} = require('googleapis');
const chat = google.chat('v1')

const auth = new google.auth.GoogleAuth({
  keyFile: 'credentials.json',
  scopes: ['https://www.googleapis.com/auth/chat.bot'],
});


boroadCastMessage = async (requestBody) => {
  const authClient = await auth.getClient();
  const response = await chat.spaces.list({ auth: authClient })
  let messages = response.data.spaces.map(space => {
    return chat.spaces.messages.create({auth:authClient,parent:space.name,requestBody }).then( res => {
        return res.data.name
    })
  });
  return Promise.all(messages);
}
getMessage = async (messageid) => {
   const authClient = await auth.getClient();
   return chat.spaces.messages.get({auth: authClient, name: messageid})
} 
updateMessages = async (messages,requestBody, updateMask = "cards") => {
  const authClient = await auth.getClient();
  let updates = messages.map( messageid => {
    return chat.spaces.messages.update({ auth: authClient,requestBody, name: messageid, updateMask: updateMask})
  })
  return Promise.all(updates)
}

module.exports = {
  boroadCastMessage,
  updateMessages,
  getMessage
}
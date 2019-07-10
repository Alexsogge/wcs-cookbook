from channels.generic.websocket import WebsocketConsumer
from asgiref.sync import async_to_sync
import json
from webportal.models import *

class ApiConsumer(WebsocketConsumer):

    activeSession = None

    def connect(self):
        self.session_id = self.scope['url_route']['kwargs']['session_id']
        self.session_group_name = 'session_%s' % self.session_id
        print("New socket connected")
        print("Scope:", self.scope)

        self.activeSession = CookingSession.objects.get(id=self.session_id)
        print("User:", type(self.scope['user']), "?==?", type(self.activeSession.owner.get_username()))
        if self.activeSession.owner == self.scope['user']:
            print("Accept user with session")
            async_to_sync(self.channel_layer.group_add)(
                self.session_group_name,
                self.channel_name
            )
            self.accept()
            msg_body = {'event': 'debug', 'message': 'New device in session.'}
            self.send_message(msg_body)
            self.send_session_update()


    def disconnect(self, close_code):
        async_to_sync(self.channel_layer.group_discard)(
            self.session_group_name,
            self.channel_name
        )

    def receive(self, text_data):
        print("Recived:", text_data)
        text_data_json = json.loads(text_data)
        message = text_data_json['message']
        print(message)



        if message == "next_step":
            self.activeSession.refresh_from_db()
            if self.activeSession.current_step < self.activeSession.recipe.work_steps.count() - 1:
                self.activeSession.current_step += 1
            self.activeSession.save()
            self.send_session_update()
        elif message == "previous_step":
            self.activeSession.refresh_from_db()
            if self.activeSession.current_step > 0:
                self.activeSession.current_step -= 1
            self.activeSession.save()
            self.send_session_update()
        elif message == "debug":
            msg_body = {'event': 'debug', 'message': text_data_json['debug']}
            self.send_message(msg_body)
        else:
            print("Unequal")

    def send_session_update(self):
        new_workstep = self.activeSession.recipe.work_steps.all()[self.activeSession.current_step]
        msg_body = {'event': 'step_update', 'new_step': self.activeSession.current_step,
                    'step_desc': new_workstep.description}
        self.send_message(msg_body)

    def send_message(self, message):
        # Send message to room group
        print("send", message)
        async_to_sync(self.channel_layer.group_send)(
            self.session_group_name,
            {
                'type': 'chat_message',
                'message': message
            }
        )

        # Receive message from room group

    def chat_message(self, event):
        message = event['message']
        print("SocketMSG:", message)

        # Send message to WebSocket
        self.send(text_data=json.dumps({
            'message': message
        }))
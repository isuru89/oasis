import React, { Component } from 'react'
import styled from 'styled-components'

const Content = styled.div`
  height: 100%;
`

const UserHeader = styled.div`
  width: 100%;
  height: 50px;
  line-height: 50px;
  background: repeating-linear-gradient(
  -45deg,
  #53A5D0,
  #53A5D0 1px,
  #53A5D0 1px,
  #53A5D0 2px
  );
  background-color: #53A5D0;
  color: #FBFCFD;
  font-size: 1.61em;
  letter-spacing: 1.2px;
`

const Title = styled.div`
  padding: 0 20px;
`


export default class ProfilePage extends Component {
  render() {
    return (
      <Content>
        <UserHeader>
          <Title>Isuru Madushanka</Title>
        </UserHeader>
      </Content>
    )
  }
}
